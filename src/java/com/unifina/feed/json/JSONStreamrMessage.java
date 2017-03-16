package com.unifina.feed.json;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.feed.AbstractStreamrMessage;

import java.nio.charset.Charset;
import java.util.*;

/**
 * JSONStreamrMessage can be a Map or a List of Maps.
 */
public class JSONStreamrMessage extends AbstractStreamrMessage {

	private static Charset utf8 = Charset.forName("UTF-8");

	private final Map map;
	private final List<Map> list;

	public JSONStreamrMessage(String streamId, int partition, Date timestamp, Date receiveTime, Map map) {
		super(streamId, partition, timestamp, receiveTime);
		this.map = map;
		this.list = null;
	}

	public JSONStreamrMessage(String streamId, int partition, Date timestamp, Date receiveTime, List<Map> list) {
		super(streamId, partition, timestamp, receiveTime);
		this.list = list;
		this.map = null;
	}

	@Override
	public FeedEvent[] toFeedEvents(IEventRecipient recipient) {
		if (map != null) {
			return new FeedEvent[] { new FeedEvent(this, getTimestamp(), recipient) };
		} else {
			FeedEvent[] events = new FeedEvent[list.size()];
			String streamId = getStreamId();
			int partition = getPartition();
			Date timestamp = getTimestamp();
			Date receiveTime = getReceiveTime();
			for (int i=0; i<events.length; i++) {
				events[i] = new FeedEvent(new JSONStreamrMessage(streamId, partition, timestamp, receiveTime, list.get(i)), timestamp, recipient);
			}
			return events;
		}
	}

	@Override
	public Set<String> keySet() {
		if (map != null) {
			return map.keySet();
		} else {
			// Don't report any keys for lists
			return Collections.emptySet();
		}
	}

	@Override
	public Object get(String key) {
		if (map != null) {
			return map.get(key);
		} else {
			throw new RuntimeException("Output value queried for a JSON message that is a list!");
		}
	}
}
