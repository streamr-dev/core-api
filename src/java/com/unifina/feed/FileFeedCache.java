package com.unifina.feed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

// OPTIMIZED

public class FileFeedCache extends Thread implements IFeedCache<String> {
	
	ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
	
	boolean quit = false;
	
	Path file = null;
	BufferedWriter writer = null;
	
	private int queuedSize = 0;
	private int flushedSize = 0;
	
	private static final Logger log = Logger.getLogger(FileFeedCache.class);
	
	public FileFeedCache(Path file) throws IOException {
		this.file = file;
		
		/**
		 * If file exists, calculate how many messages have been received already
		 * and what the next expected message sequence number should be
		 */
		if (!Files.exists(file.getParent()))
			Files.createDirectory(file.getParent());
		
		if (Files.exists(file)) {
			log.info(file+" exists, calculating number of lines..");
			BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.ISO_8859_1);
			while (reader.readLine()!=null)
				flushedSize++;
			log.info(file+" contains "+flushedSize+" cached messages.");
		}
		
		setPriority(NORM_PRIORITY-1);
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.toFile(),true)), 1024 * 1024 * 30);
		setName("FileFeedCache");
	}
	
	@Override
	public void run() {
		while (!quit) {

			try {
				Thread.sleep(1000 * 10);
			} catch (InterruptedException e) {}	
			
			// Flush queue every 10 seconds
			flush();
		}
		
		try {
			writer.close();
		} catch (IOException e) {}
		
	}

	public void quit() {
		quit = true;
	}
	
	@Override
	public void cache(Object msg) {
		queue.add(msg.toString());
		queuedSize++;
	}

	@Override
	public Catchup<String> getCatchup() {
		try {
			flush();
			return new FileCatchup(file,this);
		} catch (IOException e) {
			throw new RuntimeException("Exception while getting catchup",e);
		}
	}

	@Override
	public int getCacheSize() {
		return flushedSize;
	}

	@Override
	public void flush() {
		// Multiple threads shouldn't be here to ensure correct ordering 
		synchronized(writer) {
			String line = null;
			try {
				while (true) {
					line = queue.poll();
					if (line==null) break;

					writer.write(line+"\n");
					flushedSize++;
				}
				writer.flush();
			} catch (IOException e) {
				log.error("Failed to flush queue: ",e);
			}
		}
	}
}
