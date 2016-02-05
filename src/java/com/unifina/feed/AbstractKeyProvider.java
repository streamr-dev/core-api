package com.unifina.feed;

import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;

/**
 * Establishes a common key that can be extracted from both the subscriber object
 * as well as the messages. The key will be used to pair these together.
 * @param <Subscriber>
 * @param <Message>
 */
public abstract class AbstractKeyProvider<Subscriber, Message> {
	
	protected Globals globals;
	protected Feed feed;

	public AbstractKeyProvider(Globals globals, Feed feed) {
		this.globals = globals;
		this.feed = feed;
	}
	
	/**
	 * Extracts a key object that will be used to match incoming
	 * messages to the subscriber associated with this key.
	 * @param subscriber
	 * @return
	 */
	public abstract Object getSubscriberKey(Subscriber subscriber);
	
	/**
	 * Extracts a key from a message that can be used to find the correct 
	 * subscriber.
	 * @param message
	 * @return
	 */
	public abstract Object getMessageKey(Message message);
}
