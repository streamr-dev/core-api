package com.unifina.feed;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.unifina.domain.data.Feed;

public class FeedFactory {

	private static HashMap<Long,MessageRecipient> instanceByFeed = new HashMap<>();
	private static final Logger log = Logger.getLogger(FeedFactory.class);
	
	synchronized static MessageRecipient startInstance(Feed feed, Map<String,Object> config) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IllegalArgumentException {
		MessageRecipient instance = instanceByFeed.get(feed.getId());
		
		if (instance==null) {
			// Instantiate the MessageSource
			MessageSource source = createMessageSource(feed,config);
			return startInstance(feed, source, config);
		}
		else throw new IllegalStateException("Singleton instance for "+feed+" already started!");
	}
	
	synchronized static MessageRecipient startInstance(Feed feed, MessageSource source, Map<String,Object> config) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IllegalArgumentException {
		MessageRecipient instance = instanceByFeed.get(feed.getId());
		
		if (instance==null) {
			// Instantiate the cache
			IFeedCache cache = createCache(feed,config);
			
			// Instantiate the parser
			MessageParser parser = createParser(feed);
			
			// Instantiate the MessageRecipient
			instance = createMessageRecipient(feed,source,parser,cache);
			instanceByFeed.put(feed.getId(),instance);
			return instance;
		}
		else throw new IllegalStateException("Singleton instance for "+feed+" already started!");
	}
	
	private static IFeedCache createCache(Feed feed, Map<String,Object> config) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IllegalArgumentException {
		if (feed.getCacheClass()!=null) {
			Class cacheClass = FeedFactory.class.getClassLoader().loadClass(feed.getCacheClass());
			Constructor cacheConstructor = cacheClass.getConstructor(String.class, Map.class);
			return (IFeedCache) cacheConstructor.newInstance(feed.getCacheConfig(), config);
		}
		else {
			log.warn("Feed cache class not defined for feed "+feed.getId());
			return null;
		}
	}
	
	private static MessageParser createParser(Feed feed) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IllegalArgumentException {
		Class parserClass = FeedFactory.class.getClassLoader().loadClass(feed.getParserClass());
		return (MessageParser) parserClass.newInstance();
	}
	
	private static MessageSource createMessageSource(Feed feed, Map<String,Object> config) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IllegalArgumentException {
		Class messageSourceClass = FeedFactory.class.getClassLoader().loadClass(feed.getMessageSourceClass());
		Constructor messageSourceConstructor = messageSourceClass.getConstructor(String.class, Map.class);
		return (MessageSource) messageSourceConstructor.newInstance(feed.getMessageSourceConfig(), config);
	}
	
	private static MessageRecipient createMessageRecipient(Feed feed, MessageSource source, MessageParser parser, IFeedCache cache) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		MessageHub hub = new MessageHub(source,parser,cache);
		hub.start();
		return hub;
	}
	
	synchronized static MessageRecipient getInstance(Feed feed) {
		if (!instanceByFeed.containsKey(feed.getId()))
			throw new IllegalStateException("Singleton instance not started!");
		else return instanceByFeed.get(feed.getId());
	}

}
