package com.unifina.feed;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a Streamr message, which can be queried for values to
 * send from outputs.
 */
public abstract class AbstractStreamrMessage implements ITimestamped, Comparable<AbstractStreamrMessage> {

	private final int partition;
	private final String streamId;

	private final Date timestamp;
	private final Date receiveTime;

	public AbstractStreamrMessage(String streamId, int partition, Date timestamp, Date receiveTime) {
		this.streamId = streamId;
		this.partition = partition;
		this.timestamp = timestamp;
		this.receiveTime = receiveTime;
	}

	public int getPartition() {
		return partition;
	}

	public String getStreamId() {
		return streamId;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	@Override
	public int compareTo(AbstractStreamrMessage msg) {
		return getTimestamp().compareTo(msg.getTimestamp());
	}

	/**
	 * Returns FeedEvents for this message. The default implementation returns
	 * an array of one FeedEvent, containing 'this' as the FeedEvent content.
     */
	public FeedEvent<AbstractStreamrMessage, IEventRecipient>[] toFeedEvents(IEventRecipient recipient) {
		return new FeedEvent[] { new FeedEvent<>(this, timestamp, recipient) };
	}

	/**
	 * Should return a Set containing the key names, for which this message holds data.
     */
	public abstract Set<String> keySet();

	/**
	 * Get the value contained by this message for a given key.
	 * May return null if there is no value.
     */
	public abstract Object get(String key);

}
