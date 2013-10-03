package com.unifina.feed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

/**
 * This singleton class serves two purposes:
 * - Distribute incoming feed data (type T) to instance-specific feed proxies
 * - Preprocess raw feed data (Message -> T) to avoid repeating the same work in each feed proxy
 * @author Henri
 *
 * @param <T>
 */
public class MessageHub<T> extends Thread implements MessageRecipient {

	protected MessageSource source;
	protected MessageParser<T> parser;
	protected IFeedCache cache;
	
	protected ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<>(1000*1000);
	protected ArrayList<AbstractFeedProxy<T>> proxies = new ArrayList<>();
	
	protected AbstractFeedProxy<T>[] proxiesByPriority = new AbstractFeedProxy[0];
	protected Comparator<AbstractFeedProxy<T>> proxyPriorityComparator = new Comparator<AbstractFeedProxy<T>>() {
		@Override
		public int compare(AbstractFeedProxy<T> o1, AbstractFeedProxy<T> o2) {
			return Integer.compare(o1.getPriority(), o2.getPriority());
		}
	};
	
	private boolean quit = false;
	private static final Logger log = Logger.getLogger(MessageHub.class);
	
	protected MessageHub(MessageSource source, MessageParser<T> parser, IFeedCache cache) {
		this.source = source;
		this.cache = cache;
		this.parser = parser;
		
		source.setExpectedCounter(cache.getCacheSize()+1);
		source.setRecipient(this);
		
		setName("MsgHub_"+source.getClass().getSimpleName());
		start();
	}
	
//	public abstract T preprocess(Object msg);
	public MessageParser<T> getParser() {
		return parser;
	}
	
	@Override
	public void run() {
		while (!quit) {
			Message m;

			// Blocks if empty
			try {
				m = queue.take();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			try {
				// Preprocess here to avoid repeating something in each feed proxy
				T msg = parser.parse(m.message);

				// TODO: also filter here to avoid filtering in each proxy?

				// Distribute preprocessed message to feed proxies

				// TODO: possible ConcurrentModificationException?
				for (AbstractFeedProxy<T> p : proxiesByPriority) {
					p.receive(m.counter, msg);
				}
			} catch (Exception e) {
				log.error("Failed to handle message!",e);
			}
			
			try {
				// Deliver message to cache
				cache.cache(m.message);
			} catch (Exception e) {
				log.error("Failed to save message to cache!",e);
			}
		}
	}

	@Override
	public void receive(Message m) {
		// TODO: check needed?
		//	if (m.counter==expected) {

		if (queue.remainingCapacity()==0)
			log.warn("WARNING: Hub queue is full, producer will block!");

		try {
			queue.put(m);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void addProxy(AbstractFeedProxy<T> p) {
		synchronized(proxies) {
			if (!proxies.contains(p)) {
				proxies.add(p);
				proxiesByPriority = proxies.toArray(new AbstractFeedProxy[proxies.size()]);
				Arrays.sort(proxiesByPriority, proxyPriorityComparator);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void removeProxy(AbstractFeedProxy<T> p) {
		synchronized(proxies) {
			proxies.remove(p);
			proxiesByPriority = proxies.toArray(new AbstractFeedProxy[proxies.size()]);
			Arrays.sort(proxiesByPriority, proxyPriorityComparator);
		}
	}

	public void swapProxy(AbstractFeedProxy<T> remove, AbstractFeedProxy<T> add) {
		synchronized(proxies) {
			removeProxy(remove);
			addProxy(add);
		}
	}
	
	/**
	 * Starts the catchup and adds the proxy to the proxy list in an
	 * atomic operation, so that the messages in the catchup and subsequent
	 * messages in the eventQueue don't overlap.
	 * @param proxy
	 * @return
	 */
	public Catchup startCatchup(AbstractFeedProxy<T> proxy) {
		synchronized(proxies) {
			Catchup catchup = cache.getCatchup();
			addProxy(proxy);
			return catchup;
		}
	}
	
	public IFeedCache getCache() {
		return cache;
	}

	public MessageSource getSource() {
		return source;
	}

	// Escalate session state to proxies
	
	public void sessionBroken() {
		for (AbstractFeedProxy<T> p : proxies)
			p.sessionBroken();
	}

	public void sessionRestored() {
		for (AbstractFeedProxy<T> p : proxies)
			p.sessionRestored();
	}

	public void sessionTerminated() {
		for (AbstractFeedProxy<T> p : proxies)
			p.sessionTerminated();
	}

}
