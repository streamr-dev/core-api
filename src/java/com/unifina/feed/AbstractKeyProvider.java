package com.unifina.feed;

import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;

/**
 * Establishes a common key that can be extracted from both the subscriber object
 * as well as the messages. The key will be used to pair these together and to subscribe
 * the MessageSource.
 * @param <ModuleClass>
 * @param <MessageClass>
 * @param <KeyClass>
 */
public abstract class AbstractKeyProvider<ModuleClass, MessageClass, KeyClass> {
	
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
	public abstract KeyClass getSubscriberKey(ModuleClass subscriber);
	
	/**
	 * Extracts a key from a message that can be used to find the correct 
	 * subscriber.
	 * @param message
	 * @return
	 */
	public abstract KeyClass getMessageKey(MessageClass message);
}
