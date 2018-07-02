package com.unifina.feed;

import java.util.Date;
import java.util.Map;

import com.unifina.feed.map.MapMessage;

public class StreamrMessage extends MapMessage {

	private final int partition;
	private final String streamId;

	public StreamrMessage(String streamId, int partition, Date timestamp, Map content) {
		super(timestamp, content);
		this.streamId = streamId;
		this.partition = partition;
	}

	public int getPartition() {
		return partition;
	}

	public String getStreamId() {
		return streamId;
	}

	@Override
	public String toString() {
		return "StreamrMessage{" +
				"partition=" + partition +
				", streamId='" + streamId + '\'' +
				", timestamp=" + timestamp +
				", payload=" + payload +
				'}';
	}
}
