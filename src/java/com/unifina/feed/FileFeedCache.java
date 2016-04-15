package com.unifina.feed;

import grails.converters.JSON;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.json.JSONObject;

// OPTIMIZED

public class FileFeedCache extends Thread implements IFeedCache<String> {
	
	private SimpleDateFormat dirFormat = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat fileFormat = new SimpleDateFormat("MM-dd");
	
	ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
	
	boolean quit = false;
	
	Path file = null;
	BufferedWriter writer = null;
	
	private int queuedSize = 0;
	private int flushedSize = 0;
	
	private static final Logger log = Logger.getLogger(FileFeedCache.class);
	
	public FileFeedCache(String cacheConfig, Map<String,Object> globalConfig) throws IOException {
		
		if (!globalConfig.containsKey("unifina.fileFeedCache.recordDir"))
			throw new RuntimeException("Global config does not contain key unifina.feed.recordDir");
		
		JSONObject cc = (JSONObject) JSON.parse(cacheConfig);
		
		String recordPath = globalConfig.get("unifina.fileFeedCache.recordDir").toString();
		String filePrefix = cc.containsKey("filePrefix") ? cc.get("filePrefix").toString() : "";
		String fileSuffix = cc.containsKey("fileSuffix") ? cc.get("fileSuffix").toString() : "";
		
		Date today = new Date();
		this.file = Paths.get(recordPath, dirFormat.format(today), filePrefix+fileFormat.format(today)+fileSuffix); //"-ITCH.txt");		
		
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
		start();
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
	public void receive(Message msg) {
		queue.add((msg instanceof ParsedMessage ? ((ParsedMessage)msg).getRawMessage() : msg).toString());
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

	@Override
	public void sessionBroken() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionRestored() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sessionTerminated() {
		// TODO Auto-generated method stub
		// quit()?
	}

	@Override
	public int getReceivePriority() {
		return 100;
	}
}
