package com.unifina.feed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.service.FeedFileService;
import com.unifina.service.FeedFileService.StreamResponse;

/**
 * When you implement subclasses of this file, call the createTempFile(filename) to 
 * create temporary files!
 * @author Henri
 *
 */
public abstract class AbstractFeedPreprocessor {

	ArrayList<File> tempFiles = new ArrayList<>();
	ArrayList<Stream> foundStreams = new ArrayList<>();
	Set<String> foundNames = new HashSet<>();
	
	File tempDir;
	File tempFeedFile;
	FeedFile feedFile;

	public AbstractFeedPreprocessor() {
		
	}
	
	public AbstractFeedPreprocessor(File tempDir) {
		this.tempDir = tempDir;
	}
	
	// Not thread safe!
	public void preprocess(FeedFile feedFile, FeedFileService feedFileService) {
		this.feedFile = feedFile;
		InputStream inputStream = null;
		try {
			StreamResponse response = feedFileService.getFeed(feedFile);
			if (!response.getSuccess())
				throw new FileNotFoundException("FeedFile not found: "+feedFile.getName());
			
			// Create temporary local directory
			if (tempDir==null)
				tempDir = Files.createTempDirectory(feedFile.getName()).toFile();
			
			if (!response.getIsFile()) {
				// If the file is not on local machine, first copy it to temp directory to avoid long http request
				tempFeedFile = new File(tempDir, feedFile.getName());

				FileOutputStream fileOut = new FileOutputStream(tempFeedFile);
				FileChannel fileChannel = fileOut.getChannel();

				ReadableByteChannel inChannel = Channels.newChannel(response.getInputStream());
				fileChannel.transferFrom(inChannel, 0L, Long.MAX_VALUE);
				inChannel.close(); // closes the InputStream too
				fileChannel.close();
				fileOut.close();
				inputStream = new FileInputStream(tempFeedFile);
			}
			else inputStream = response.getInputStream();
			
			// Uncompress on the fly if the stream is flagged as compressed
			if (response.getIsCompressed())
				inputStream = new GZIPInputStream(inputStream);
			
			// Preprocess the stream
			preprocess(inputStream, feedFile.getName());
			
			for (File f : tempFiles)
				feedFileService.submitPreprocessedFile(f, feedFile);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (inputStream!=null)
				try { inputStream.close(); } catch (IOException e) {}
			
			// Clean up
			for (File f : tempFiles)
				f.delete();
			
			if (tempFeedFile!=null)
				tempFeedFile.delete();
		}
		
	}
	
	/**
	 * Call this method from subclasses to create the preprocessed files!
	 * @param filename
	 * @return
	 */
	protected File createTempFile(String filename) {
		File file = new File(tempDir, filename);
		tempFiles.add(file);
		return file;
	}
	
	/**
	 * This method can be called from the subclass when a Stream is detected
	 * by the preprocessor. The list of Streams can be later retrieved via
	 * getFoundStreams().
	 * 
	 * The subclass should set the fields of the Stream object (the Feed is set
	 * by the parent implementation).
	 * @param stream
	 */
	protected void streamFound(Stream stream) {
		stream.setFeed(feedFile.getFeed());
		if (!foundNames.contains(stream.getName())) {
			foundNames.add(stream.getName());
			foundStreams.add(stream);
		}
	}
	
	/**
	 * Returns the list of Streams found while preprocessing the FeedFile.
	 */
	public List<Stream> getFoundStreams() {
		return foundStreams;
	}
	
	protected abstract void preprocess(InputStream inputStream, String name) ;

	public abstract String getPreprocessedFileName(String name, Stream stream, boolean compressed);

}
