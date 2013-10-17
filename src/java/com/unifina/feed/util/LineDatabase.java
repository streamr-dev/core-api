package com.unifina.feed.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class LineDatabase {
	
//	private RandomAccessFile raf;
	
	private int flushedSize = 0;
	private int linesPendingFlush = 0;
	private int writtenLineLength = 0;
	
	private BufferedWriter writer;

	private Path file;
	private int indexInterval;
	
	// Start of line i*indexInterval is at index.get(i) bytes offset
	private ArrayList<Integer> index = new ArrayList<Integer>();
	
	private static final Logger log = Logger.getLogger(LineDatabase.class);
	
	public LineDatabase(Path file, int indexInterval) throws FileNotFoundException, IOException {
		this.file = file;
		this.indexInterval = indexInterval;
		
		/**
		 * If file exists, calculate how many messages have been received already
		 * and what the next expected message sequence number should be
		 */
		if (!Files.exists(file.getParent()))
			Files.createDirectory(file.getParent());
		
		if (!Files.exists(file))
			Files.createFile(file);
		
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.toFile(),true)), 1024 * 1024 * 30);
		
		log.info("Calculating number of lines in "+file);
		
//		raf = new RandomAccessFile(file.toFile(), "r");
		init(file);
		
		log.info(file+" contains "+flushedSize+" cached messages.");
	}
	
	private void init(Path file) throws IOException {
		String l;
		flushedSize = 0;
		index.add(0);
		int lengthCount = 0;
		
		BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.ISO_8859_1);
		while ((l = reader.readLine())!=null) {
			flushedSize++;
			lengthCount += l.length()+1;
			if (flushedSize % indexInterval == 0) {
				index.add(lengthCount);
			}
		}
		
//		while ((l=raf.readLine())!=null) {
//			flushedSize++;
//			lengthCount += l.length()+1;
//			if (flushedSize % indexInterval == 0) {
//				index.add(lengthCount);
//			}
//		}
		reader.close();
	}
	
	public synchronized void addLine(String line) throws IOException {
		writer.write(line);
		writer.write("\n");
		linesPendingFlush++;
		writtenLineLength += line.length()+1;
		if ((flushedSize+linesPendingFlush) % indexInterval == 0) {
			index.add(writtenLineLength);
			writtenLineLength = 0;
		}
	}
	
	public synchronized void flush() throws IOException {
		writer.flush();
		flushedSize += linesPendingFlush;
		linesPendingFlush = 0;
	}
	
	public /*synchronized*/ List<String> getLines(long start, int count) throws IOException {
		// Read to the nearest indexed location
		flush();
		
		// If not reading sequence, we have to lookup the position
		int i = (int) start / indexInterval;
		if (i>=index.size()) {
			return new ArrayList<String>(0);
		}
		else {
			long line = i*indexInterval+1;
			FileInputStream fis = new FileInputStream(file.toFile());
			fis.skip(index.get(i));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.ISO_8859_1));
			
			// Seek to the start position
			String s = br.readLine();
			while (s!=null && line < start) {
				line++;
				s = br.readLine();
			}
			ArrayList<String> result = new ArrayList<>(Math.min(count,10000));
			while (s!=null && result.size()<count) {
				result.add(s);
				line++;
				s = br.readLine();
			}
			br.close();
			return result;
		}
	}
	
	public synchronized void close() throws IOException {
		flush();
		writer.close();
	}
	
	public synchronized long size() throws IOException {
		flush();
		return flushedSize;
	}
}
