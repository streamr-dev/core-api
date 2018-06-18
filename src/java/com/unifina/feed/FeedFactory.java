package com.unifina.feed;

import com.unifina.domain.data.Feed;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to help create singleton MessageSources and associated MessageHubs to be used
 * with/by feed implementations that extend AbstractFeedProxy.
 */
public class FeedFactory {
	private static Map<Long, MessageHub> instanceByFeed = new HashMap<>();

	synchronized static MessageHub getInstance(Feed feed, Map<String,Object> config) throws InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		if (!instanceByFeed.containsKey(feed.getId())) {
			startInstance(feed, config);
		}
		return instanceByFeed.get(feed.getId());
	}

	public static synchronized MessageHub getRunningInstance(Feed feed) {
		return instanceByFeed.get(feed.getId());
	}

	/**
	 * For testing
	 */
	public synchronized static void stopAndClearAll() {
		for (Long key : instanceByFeed.keySet()) {
			instanceByFeed.get(key).quit();
		}
		instanceByFeed.clear();
	}

	private synchronized static void startInstance(Feed feed, Map<String,Object> config) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IllegalArgumentException {
		if (!instanceByFeed.containsKey(feed.getId())) {
			MessageSource source = createMessageSource(feed, config);
			IFeedCache cache = createCache(feed, config);
			MessageParser parser = createParser(feed);

			MessageHub instance = createAndStartMessageHub(source, parser, cache);
			instanceByFeed.put(feed.getId(), instance);
		} else {
			throw new IllegalStateException("Singleton instance for " + feed + " already started!");
		}
	}

	private static MessageSource createMessageSource(Feed feed, Map<String,Object> config) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IllegalArgumentException {
		Class messageSourceClass = FeedFactory.class.getClassLoader().loadClass(feed.getMessageSourceClass());
		Constructor messageSourceConstructor = messageSourceClass.getConstructor(Feed.class, Map.class);
		return (MessageSource) messageSourceConstructor.newInstance(feed, config);
	}
	
	private static IFeedCache createCache(Feed feed, Map<String,Object> config) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IllegalArgumentException {
		if (feed.getCacheClass() != null) {
			Class cacheClass = FeedFactory.class.getClassLoader().loadClass(feed.getCacheClass());
			Constructor cacheConstructor = cacheClass.getConstructor(String.class, Map.class);
			return (IFeedCache) cacheConstructor.newInstance(feed.getCacheConfig(), config);
		} else {
			return null;
		}
	}
	
	private static MessageParser createParser(Feed feed) throws ClassNotFoundException, IllegalAccessException, InstantiationException, IllegalArgumentException {
		Class parserClass = FeedFactory.class.getClassLoader().loadClass(feed.getParserClass());
		return (MessageParser) parserClass.newInstance();
	}
	
	private static MessageHub createAndStartMessageHub(MessageSource source, MessageParser parser, IFeedCache cache) throws SecurityException, IllegalArgumentException {
		MessageHub hub = new MessageHub(source, parser, cache);
		hub.start();
		return hub;
	}
}
