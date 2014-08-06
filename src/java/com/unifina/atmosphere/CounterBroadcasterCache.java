package com.unifina.atmosphere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterCache;

public class CounterBroadcasterCache implements BroadcasterCache {

	ArrayList<Object> cache = new ArrayList<Object>();
	
	int counter = 0;
	int missed = 0;
//	private static final String COUNTER_KEY = "counter";
	private static final int MAX_CHUNK = 30000;
	
	private HashMap<Object,Integer> cacheIdMap = new HashMap<>();
	private static final String PURGED_STRING = "{},";
	
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

	public synchronized void add(String msg, int c, Object cacheId) {
		// New message
		if (cache.size()+missed==c) {
			cache.add(msg);
			
			if (cacheId!=null) {
				Integer previous = cacheIdMap.put(cacheId, cache.size()-1);
				// If the key already existed in the cache, set the index to null to free up the object
				if (previous!=null)
					cache.set(previous, PURGED_STRING);
			}
		}
		else if (cache.size()>c) {
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
		
		if (c-missed>=cache.size())
			return null;
		else {
			// TODO: fix hack: for small responses set content type to application/x-json because that mime type will not be compressed by tomcat
			int startIndex = c-missed;
			int endIndex = Math.min(c-missed+MAX_CHUNK,cache.size());
			if (endIndex-startIndex < 100)
				r.getResponse().setContentType("application/x-json");
			
			return new ArrayList<Object>(cache.subList(startIndex, endIndex));
		}
	}

//	class CachedMessage {
//		public int counter;
//		public Object message;
//		public CachedMessage(int counter, Object message) {
//			this.counter = counter;
//			this.message = message;
//		}
//	}
	
}
