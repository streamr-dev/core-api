package com.unifina.feed;

import java.io.File;
import java.io.FileInputStream;
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

import org.apache.log4j.Logger;

import com.unifina.domain.data.FeedFile;
import com.unifina.domain.data.Stream;
import com.unifina.service.FeedFileService;
import com.unifina.service.FeedService;

/**
 * When you implement subclasses of this file, call the createTempFile(filename) to 
 * create temporary files!
 * @author Henri
 *
 */
public abstract class AbstractFeedPreprocessor {

	List<File> tempFiles = new ArrayList<>();
	List<Stream> foundStreams = new ArrayList<>();
	Set<String> foundNames = new HashSet<>();
	
	File tempDir;
	File tempFeedFile;
	FeedFile feedFile;
	
	private static final Logger log = Logger.getLogger(AbstractFeedPreprocessor.class);

	public AbstractFeedPreprocessor() {
		
	}
	
	public AbstractFeedPreprocessor(File tempDir) {
		this.tempDir = tempDir;
	}
	
	/**
	 * Processes the FeedFile from a custom InputStream.
	 * This method is not thread safe.
	 * @param feedFile
	 * @param feedFileService
	 */
	public void preprocess(FeedFile feedFile, FeedFileService feedFileService, FeedService feedService, InputStream inputStream, boolean isCompressed, boolean saveToDiskFirst) {
		this.feedFile = feedFile;

		try {
			// Create temporary local directory
			if (tempDir==null)
				tempDir = Files.createTempDirectory(feedFile.getName()).toFile();
			
			if (saveToDiskFirst) {
				// If the file is not on local machine, first copy it to temp directory to avoid long http request
				tempFeedFile = new File(tempDir, feedFile.getName());

				log.info("Saving feed file to "+tempFeedFile+" for processing...");
				
				FileOutputStream fileOut = new FileOutputStream(tempFeedFile);
				FileChannel fileChannel = fileOut.getChannel();

				ReadableByteChannel inChannel = Channels.newChannel(inputStream);
				fileChannel.transferFrom(inChannel, 0L, Long.MAX_VALUE);
				inChannel.close(); // closes the InputStream too
				fileChannel.close();
				fileOut.close();
				inputStream = new FileInputStream(tempFeedFile);
			}
			else {
				log.info("Preprocessing feed file on the fly: "+feedFile.getName());
			}
			
			// Decompress on the fly if the source is compressed
			if (isCompressed) {
				inputStream = new GZIPInputStream(inputStream);
				log.info("Handling input stream as GZipped: "+feedFile.getName());
			}
			else log.info("Handling input stream as uncompressed: "+feedFile.getName());
			
			// Preprocess the stream
			log.info("Preprocessing "+feedFile.getName()+"...");
			preprocess(inputStream, feedFile.getName());
			
			// Submit the preprocessed files
			log.info("Storing "+tempFiles.size()+" files from "+feedFile.getName());
			for (File f : tempFiles)
				feedFileService.storeFile(f, feedFile);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (inputStream!=null)
				try { inputStream.close(); } catch (IOException e) {}
			
			// Clean up
			log.info("Cleaning up "+tempFiles.size()+" temporary files from "+feedFile.getName());
			for (File f : tempFiles)
				f.delete();
			
			if (tempFeedFile!=null) {
				log.info("Deleting temporary feed file: "+tempFeedFile);
				tempFeedFile.delete();
			}
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
	
	public List<File> getPreprocessedFiles() {
		return tempFiles;
	}
	
	protected abstract void preprocess(InputStream inputStream, String name) ;

	public abstract String getPreprocessedFileName(String name, Stream stream, boolean compressed);

}
