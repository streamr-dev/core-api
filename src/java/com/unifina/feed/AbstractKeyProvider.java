package com.unifina.feed;

import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;

public abstract class AbstractKeyProvider {
	
	protected Globals globals;
	protected Feed feed;

	public AbstractKeyProvider(Globals globals, Feed feed) {
		this.globals = globals;
		this.feed = feed;
	}
	
	/**
	 * Extracts a key object that will be used to match incoming
	 * messages to the IEventRecipient associated with this key.
	 * @param subscriber
	 * @return
	 */
	public abstract Object getSubscriberKey(Object subscriber);
	
	/**
	 * Extracts a key from a message that can be used to find the correct 
	 * IEventRecipient.
	 * @param message
	 * @return
	 */
	public abstract Object getMessageKey(Object message);
}
