package com.unifina.feed.util;

import com.streamr.client.protocol.message_layer.StreamMessage;

import java.io.Serializable;
import java.util.Comparator;

public class StreamMessageComparator implements Comparator<StreamMessage>, Serializable {
	@Override
	public int compare(StreamMessage o1, StreamMessage o2) {
		int ts = Long.compare(o1.getTimestamp(), o2.getTimestamp());
		return ts != 0 ? ts : Long.compare(o1.getSequenceNumber(), o2.getSequenceNumber());
	}
}
