package com.unifina.feed;

import java.util.Date;
import java.util.Map;

import com.unifina.data.StreamrBinaryMessage;
import com.unifina.feed.map.MapMessage;

public class StreamrMessage extends MapMessage {

	private final int partition;
	private final String streamId;
	private final byte signatureType;
	private final String address;
	private final String signature;

	public StreamrMessage(String streamId, int partition, Date timestamp, Map content) {
		super(timestamp, content);
		this.streamId = streamId;
		this.partition = partition;
		this.signatureType = StreamrBinaryMessage.SIGNATURE_TYPE_NONE;
		this.address = null;
		this.signature = null;
	}

	public StreamrMessage(String streamId, int partition, Date timestamp, Map content, byte signatureType, String address, String signature) {
		super(timestamp, content);
		this.streamId = streamId;
		this.partition = partition;
		this.signatureType = signatureType;
		this.address = address;
		this.signature = signature;
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
