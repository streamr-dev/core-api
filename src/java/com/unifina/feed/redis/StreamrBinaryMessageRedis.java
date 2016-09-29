package com.unifina.feed.redis;

import com.unifina.data.StreamrBinaryMessage;

import java.nio.ByteBuffer;

/**
 * Extends StreamrBinaryMessage by appending Kafka offset and partition information.
 * Suffixes the StreamrBinaryMessage binary with
 * - version 1 byte
 * - offset 8 bytes (long)
 * - previousOffset 8 bytes (long)
 * - partition 4 bytes (int)
 */
public class StreamrBinaryMessageRedis extends StreamrBinaryMessage {

	private static final byte VERSION = 0;

	private final long offset;
	private final Long previousOffset;
	private final int kafkaPartition;

	public StreamrBinaryMessageRedis(ByteBuffer bb) {
		super(bb);

		byte version = bb.get();
		if (version == 0) {
			offset = bb.getLong();
			previousOffset = bb.getLong();
			kafkaPartition = bb.getInt();
		}
		else throw new IllegalArgumentException("Invalid version: "+version);
	}

	public StreamrBinaryMessageRedis(String streamId, int streamPartition, long timestamp, int ttl, byte contentType, byte[] content, int kafkaPartition, long offset, Long previousOffset) {
		super(streamId, streamPartition, timestamp, ttl, contentType, content);
		this.offset = offset;
		this.previousOffset = previousOffset;
		this.kafkaPartition = kafkaPartition;
	}

	public long getOffset() {
		return offset;
	}

	public Long getPreviousOffset() {
		return previousOffset >= 0 ? previousOffset : null;
	}

	public int getKafkaPartition() {
		return kafkaPartition;
	}

	@Override
	public byte[] toBytes() {
		byte[] orig = super.toBytes();
		ByteBuffer bb = ByteBuffer.allocate(orig.length + 21);
		bb.put(orig);
		bb.put(VERSION); // 1
		bb.putLong(offset); // 8
		bb.putLong(previousOffset != null ? previousOffset : -1); // 8
		bb.putInt(kafkaPartition); // 4
		return bb.array();
	}
}
