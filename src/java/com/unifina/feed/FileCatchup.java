package com.unifina.feed;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

public class FileCatchup implements Catchup<String> {

	BufferedReader reader;
	FileFeedCache cache;
	int counter = 0;
	
	private static final Logger log = Logger.getLogger(FileCatchup.class);
	
	public FileCatchup(Path file, FileFeedCache cache) throws IOException {
		reader = Files.newBufferedReader(file, StandardCharsets.ISO_8859_1);
		this.cache = cache;
	}
	
	@Override
	public String getNext() {
		try {
			String line = reader.readLine();
			
			// Try flushing the cache and re-reading
			if (line==null) {
				cache.flush();
				line = reader.readLine();
			}
			
			// If really no more lines in file, we are ready!
			if (line==null) {
//				System.out.println("Caught up "+counter+" messages.");
				return null;
			}

			counter++;
			
			return line;
		} catch (Exception e) {
			log.error("Error in getNext()",e);
			return null;
		}
	}

	@Override
	public int getNextCounter() {
		return counter;
	}
	
}
