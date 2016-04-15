package com.unifina.feed;

import java.util.List;

/**
 * Created by henripihkala on 11/02/16.
 */
public abstract class Poller<MessageClass, KeyClass> {
	/**
	 * @return a list of all new MessageClass instances since last poll
     */
	public abstract List<Message<MessageClass, KeyClass>> poll();

	/**
	 * @return poll interval in milliseconds
     */
	public abstract long getPollInterval();
	public abstract void close();
}
