package com.unifina.feed;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

/**
 * This singleton class serves two purposes:
 * - Distribute incoming feed data (type RawMessageClass) to instance-specific feed proxies
 * - Preprocess raw feed data (Message -> MessageClass) to avoid repeating the same work in each feed proxy
 * @author Henri
 *
 * @param <RawMessageClass>
 * @param <MessageClass>
 * @param <KeyClass>
 */
public class MessageHub<RawMessageClass, MessageClass, KeyClass> extends Thread implements MessageRecipient {
	private static final Logger log = Logger.getLogger(MessageHub.class);

	private final MessageSource source;
	private final MessageParser<RawMessageClass, MessageClass> parser;
	private final IFeedCache cache;

	private final BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1000*1000);
	private final List<MessageRecipient> proxies = new ArrayList<>();
	private final List<MessageRecipient> proxiesByPriority = new ArrayList<>();
	private final Map<KeyClass, List<MessageRecipient>> proxiesByKey = new HashMap<>();

	private final Comparator<MessageRecipient> proxyPriorityComparator = new Comparator<MessageRecipient>() {
		@Override
		public int compare(MessageRecipient o1, MessageRecipient o2) {
			return Integer.compare(o1.getReceivePriority(), o2.getReceivePriority());
		}
	};

	private boolean quit = false;

	
	MessageHub(MessageSource source, MessageParser<RawMessageClass, MessageClass> parser, IFeedCache cache) {
		this.source = source;
		this.parser = parser;
		this.cache = cache;

		source.setRecipient(this);
		if (cache != null) {
			addRecipient(cache);
		}
		
		setName("MsgHub_" + source.getClass().getSimpleName());
		// not safe to start Thread in constructor! Started in FeedFactory
	}
	
	@Override
	public void run() {
		while (!quit) {
			Message<MessageClass, KeyClass> m;

			// Blocks if empty
			try {
				m = queue.take();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			ParsedMessage<MessageClass, KeyClass> parsedMessage = null;
			try {
				// Preprocess here to avoid repeating something in each feed proxy
				parsedMessage = new ParsedMessage<>(m.counter, parser.parse((RawMessageClass) m.message), m.checkCounter);
			} catch (Exception e) {
				log.error("Failed to parse message " + m.message.toString(), e);
			}

			if (parsedMessage != null) {
				try {
					// If the message contains a key, distribute to subscribers for that key only
					List<MessageRecipient> proxyList = m.key != null ? proxiesByKey.get(m.key) : proxiesByPriority;
					for (MessageRecipient p : proxyList) {
						p.receive(parsedMessage);
					}
				} catch (Throwable e) {
					log.error("Failed to handle message!", e);
				}
			}

			synchronized (this) {
				notifyAll();
			}
		}
	}

	@Override
	public void receive(Message m) {
		if (queue.remainingCapacity() == 0) {
			log.warn("WARNING: Hub queue is full, producer will block!");
		}
		try {
			queue.put(m);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public void addRecipient(MessageRecipient p) {
		synchronized (proxies) {
			if (!proxies.contains(p)) {
				proxies.add(p);
				proxiesByPriority.add(p);
				Collections.sort(proxiesByPriority, proxyPriorityComparator);
			}
		}
	}

	public void removeRecipient(MessageRecipient p) {
		synchronized (proxies) {
			proxies.remove(p);
			proxiesByPriority.remove(p);
			Collections.sort(proxiesByPriority, proxyPriorityComparator);
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
		if (cache != null) {
			synchronized(proxies) {
				Catchup catchup = cache.getCatchup();
				addRecipient(proxy);
				return catchup;
			}
		} else {
			return null;
		}
	}

	public MessageSource getSource() {
		return source;
	}

	public MessageParser<RawMessageClass, MessageClass> getParser() {
		return parser;
	}

	// Escalate session state to proxies
	
	public void sessionBroken() {
		for (MessageRecipient p : proxiesByPriority) {
			p.sessionBroken();
		}
	}

	public void sessionRestored() {
		for (MessageRecipient p : proxiesByPriority) {
			p.sessionRestored();
		}
	}

	public void sessionTerminated() {
		for (MessageRecipient p : proxiesByPriority) {
			p.sessionTerminated();
		}
	}

	@Override
	public int getReceivePriority() {
		return 0;
	}

	/**
	 * Lets the hub know that the given MessageRecipient is interested in messages with
	 * the given key. The hub requests the underlying MessageSource to route messages
	 * with the given key to the given MessageRecipient.
	 * @param key
	 * @param proxy
	 */
	public void subscribe(KeyClass key, MessageRecipient proxy) {
		synchronized (proxiesByKey) {
			if (!proxiesByKey.containsKey(key)) {
				proxiesByKey.put(key, new ArrayList<MessageRecipient>());
			}
		}
		
		List<MessageRecipient> list = proxiesByKey.get(key);
		
		synchronized (list) {
			if (!list.contains(proxy)) {
				list.add(proxy);
			}
			
			Collections.sort(list, proxyPriorityComparator);
			
			source.subscribe(key);
		}
	}
	
	public void unsubscribe(Object key, MessageRecipient proxy) {
		List<MessageRecipient> list = proxiesByKey.get(key);
		
		if (list != null) {
			synchronized (list) {
				list.remove(proxy);
				if (list.isEmpty()) {
					log.info("unsubscribe: No more MessageRecipients for key " + key + ", unsubscribing from source");
					source.unsubscribe(key);
				}
			}
		}
	}

	public void quit() {
		quit = true;
	}
}
