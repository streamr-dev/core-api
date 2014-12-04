package com.unifina.atmosphere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterCache;

public class CounterBroadcasterCache implements BroadcasterCache {

	ArrayListWithRemoveMany<Object> cache = new ArrayListWithRemoveMany<Object>();
	
	int counter = 0;
	int missed = 0;
	int removed = 0;
	int cacheSize = 0;
	private boolean removeConsumed = false;

	private static final int MAX_CHUNK = 15000;
	private static final int MAX_SIZE = 2*MAX_CHUNK;
	private static final long MAX_BLOCK_TIME = 60*1000;
	
	private HashMap<Object,Integer> cacheIdMap = new HashMap<>();
	static final String PURGED_STRING = "{},";
	
	private static final Logger log = Logger.getLogger(CounterBroadcasterCache.class);
	
	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public /*synchronized*/ void addToCache(AtmosphereResource r, Object e) {
		/**
		 * Don't trust Atmosphere to add all messages in correct order.
		 * Instead messages are added to cache prior to broadcasting in
		 * SignalPathReturnChannel.
		 */
	}

	public synchronized void add(String msg, int c, Object cacheId) throws TimeoutException {
		// Block if the cache is full!
		while (removeConsumed && cache.size()>=MAX_SIZE)
			try {
				log.warn("Cache full, waiting for consumption...");
				wait(MAX_BLOCK_TIME);
				if (cache.size()>=MAX_SIZE)
					throw new TimeoutException("UI message consumption timed out! Connection lost?");
			} catch (InterruptedException e) {
				log.warn("Interrupted", e);
			}
		
		// New message
		if (cacheSize==c) {
			cache.add(msg);
			cacheSize++;
			
			if (cacheId!=null) {
				// Replace the value for this cacheId key
				Integer previous = cacheIdMap.put(cacheId, cacheSize-1);
				
				// Free up the previous object if it stil exists in the cache
				if (previous!=null) {
					int idx = getCounterIndex(previous);
					if (idx>=0)
						cache.set(previous, PURGED_STRING);
				}	
			}
		}
		else if (cacheSize>c) {
			System.out.println("addToCache: old message, counter is "+c+", expected:"+cache.size());
		}
		else {
			System.out.println("addToCache: messages lost, counter is "+c+", expected:"+(cache.size()+missed));
			missed++;
		}
	}
	
	@Override
	public synchronized List<Object> retrieveFromCache(AtmosphereResource r) {
		int c = 0;
		String s = r.getRequest().getHeader("X-C");
		if (s!=null && !s.isEmpty()) {
			c = Integer.parseInt(s);
		}
		else {
			s = r.getRequest().getParameter("X-C");
			if (s!=null && !s.isEmpty()) {
				c = Integer.parseInt(s);
			}
		}
		
		if (c>=cacheSize)
			return null;
		else {
			int startCounter = c;
			int endCounter = Math.min(startCounter+MAX_CHUNK, cacheSize-1);
			
			List<Object> result = getCounterRange(startCounter, endCounter);
			if (removeConsumed) {
				removeUpToCounter(endCounter);
			}
			
			// TODO: fix hack: for small responses set content type to application/x-json because that mime type will not be compressed by tomcat
			if (result.size()<100)
				r.getResponse().setContentType("application/x-json");
			
			return result;
		}
	}
	
	public synchronized void removeUpToCounter(int counter) {
		int idx = getCounterIndex(counter);
		cache.removeUpToIndex(idx);
		removed += idx+1;
		
		// add() may be blocking for space in the queue
		notify();
	}
	
	public synchronized Integer getCounterIndex(int counter) {
		return counter - missed - removed;
	}
	
	public synchronized List<Object> getCounterRange(int startCounter, int endCounter) {
		int startIndex = getCounterIndex(startCounter);
		int endIndex = getCounterIndex(endCounter);
		if (startIndex < 0) {
			log.warn("Counter "+startCounter+" maps to negative index "+startIndex);
			startIndex = 0;
		}
		if (endIndex >= cache.size()) {
			log.warn("Counter "+startCounter+" maps to an index larger than the cache head: "+endIndex+", cache size: "+cache.size());
			endIndex = cache.size()-1;
		}
		return getRange(startIndex, endIndex);
	}
	
	public synchronized List<Object> getRange(int startIndex, int endIndex) {
		return new ArrayList<Object>(cache.subList(startIndex, endIndex+1));
	}

	public void setRemoveConsumed(boolean removeConsumed) {
		this.removeConsumed = removeConsumed;
	}
	
	class ArrayListWithRemoveMany<T> extends ArrayList<T> {
		public void removeUpToIndex(int idx) {
			removeRange(0, idx+1);
		}
	}
}
