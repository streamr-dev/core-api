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
public class MessageHub<R,T> extends Thread implements MessageRecipient {

	protected MessageSource source;
	protected MessageParser<R,T> parser;
	protected IFeedCache cache;
	
	protected ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<>(1000*1000);
	protected ArrayList<MessageRecipient> proxies = new ArrayList<>();
	
	protected MessageRecipient[] proxiesByPriority = new MessageRecipient[0];
	protected Comparator<MessageRecipient> proxyPriorityComparator = new Comparator<MessageRecipient>() {
		@Override
		public int compare(MessageRecipient o1, MessageRecipient o2) {
			return Integer.compare(o1.getReceivePriority(), o2.getReceivePriority());
		}
	};
	
	private boolean quit = false;
	private static final Logger log = Logger.getLogger(MessageHub.class);
	
	protected MessageHub(MessageSource source, MessageParser<R,T> parser, IFeedCache cache) {
		this.source = source;
		this.cache = cache;
		this.parser = parser;
		
		if (cache!=null)
			source.setExpectedCounter(cache.getCacheSize()+1);
		
		source.setRecipient(this);
		if (cache!=null)
			addRecipient(cache);
		
		setName("MsgHub_"+source.getClass().getSimpleName());
//		start(); not safe to start Thread in constructor! Started in FeedFactory
	}
	
//	public abstract T preprocess(Object msg);
	public MessageParser<R,T> getParser() {
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
			
			Message parsedMessage = null;
			try {
				// Preprocess here to avoid repeating something in each feed proxy
				parsedMessage = new Message(m.counter, parser.parse((R) m.message), m.message);
				parsedMessage.checkCounter = m.checkCounter; 				// TODO: make cleaner
			} catch (Exception e) {
				log.error("Failed to parse message "+m.message.toString(),e);
			}
			
			if (parsedMessage!=null) try {
				// Distribute preprocessed message to recipients
				for (MessageRecipient p : proxiesByPriority) {
					p.receive(parsedMessage);
				}
			} catch (Exception e) {
				log.error("Failed to handle message!",e);
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
	public void addRecipient(MessageRecipient p) {
		synchronized(proxies) {
			if (!proxies.contains(p)) {
				proxies.add(p);
				proxiesByPriority = proxies.toArray(new MessageRecipient[proxies.size()]);
				Arrays.sort(proxiesByPriority, proxyPriorityComparator);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void removeRecipient(MessageRecipient p) {
		synchronized(proxies) {
			proxies.remove(p);
			proxiesByPriority = proxies.toArray(new MessageRecipient[proxies.size()]);
			Arrays.sort(proxiesByPriority, proxyPriorityComparator);
		}
	}

	public void swapRecipient(MessageRecipient remove, MessageRecipient add) {
		synchronized(proxies) {
			removeRecipient(remove);
			addRecipient(add);
		}
	}
	
	/**
	 * Starts the catchup and adds the proxy to the proxy list in an
	 * atomic operation, so that the messages in the catchup and subsequent
	 * messages in the eventQueue don't overlap.
	 * @param proxy
	 * @return
	 */
	public Catchup startCatchup(MessageRecipient proxy) {
		if (cache!=null) {
			synchronized(proxies) {
				Catchup catchup = cache.getCatchup();
				addRecipient(proxy);
				return catchup;
			}
		}
		else return null;
	}
	
	public IFeedCache getCache() {
		return cache;
	}

	public MessageSource getSource() {
		return source;
	}

	// Escalate session state to proxies
	
	public void sessionBroken() {
		for (MessageRecipient p : proxiesByPriority)
			p.sessionBroken();
	}

	public void sessionRestored() {
		for (MessageRecipient p : proxiesByPriority)
			p.sessionRestored();
	}

	public void sessionTerminated() {
		for (MessageRecipient p : proxiesByPriority)
			p.sessionTerminated();
	}

	@Override
	public int getReceivePriority() {
		return 0;
	}

	public void subscribe(Object subscriber) {
		source.subscribe(subscriber);
	}

}
