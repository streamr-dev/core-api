package com.unifina.feed.redis;

import com.unifina.data.StreamrBinaryMessage;
import com.unifina.data.StreamrBinaryMessageFactory;
import com.unifina.data.StreamrBinaryMessageV29;

import java.nio.ByteBuffer;

/**
 * Extends StreamrBinaryMessage by appending Kafka offset and partition information.
 * Suffixes the StreamrBinaryMessage binary with
 * - version 1 byte
 * - offset 8 bytes (long)
 * - previousOffset 8 bytes (long)
 * - partition 4 bytes (int)
 */
public class StreamrBinaryMessageWithKafkaMetadata {

	private static final byte VERSION = 0;
	private final StreamrBinaryMessage msg;
	private final long offset;
	private final Long previousOffset;
	private final int kafkaPartition;

	/**
	 * Creates a new StreamrBinaryMessageWithKafkaMetadata from a byte buffer
	 * obtained from toBytesWithKafkaMetadata()
	 */
	public StreamrBinaryMessageWithKafkaMetadata(ByteBuffer bb) {
		msg = StreamrBinaryMessageFactory.fromBytes(bb);

		byte version = bb.get();
		if (version == 0) {
			offset = bb.getLong();
			previousOffset = bb.getLong();
			kafkaPartition = bb.getInt();
		}
		else throw new IllegalArgumentException("Invalid version: "+version);
	}

	/**
	 * Creates a new StreamrBinaryMessageWithKafkaMetadata from a byte buffer
	 * obtained from (super.)toBytes(), adding the Kafka metadata using the other args
	 */
	public StreamrBinaryMessageWithKafkaMetadata(ByteBuffer parentBytes, int kafkaPartition, long offset, Long previousOffset) {
		msg = StreamrBinaryMessageFactory.fromBytes(parentBytes);
		this.offset = offset;
		this.previousOffset = previousOffset;
		this.kafkaPartition = kafkaPartition;
	}

	/**
	 * Creates a new StreamrBinaryMessageWithKafkaMetadata from an existing StreamrBinaryMessage
	 * adding the Kafka metadata using the other args
	 */
	public StreamrBinaryMessageWithKafkaMetadata(StreamrBinaryMessage original, int kafkaPartition, long offset, Long previousOffset) {
		msg = original;
		this.offset = offset;
		this.previousOffset = previousOffset;
		this.kafkaPartition = kafkaPartition;
	}

	/**
	 * Creates a new StreamrBinaryMessageWithKafkaMetadata using given values.
	 */
	public StreamrBinaryMessageWithKafkaMetadata(String streamId, int streamPartition, long timestamp, int ttl, byte contentType, byte[] content, int kafkaPartition, long offset, Long previousOffset) {
		msg = new StreamrBinaryMessageV29(streamId, streamPartition, timestamp, ttl, contentType, content, StreamrBinaryMessage.SignatureType.SIGNATURE_TYPE_NONE, (String) null, null);
		this.offset = offset;
		this.previousOffset = previousOffset;
		this.kafkaPartition = kafkaPartition;
	}

	/**
	 * Creates a new StreamrBinaryMessageWithKafkaMetadata using given values.
	 */
	public StreamrBinaryMessageWithKafkaMetadata(String streamId, int streamPartition, long timestamp, int ttl, byte contentType,
												 byte[] content, StreamrBinaryMessage.SignatureType signatureType, String address, String signature, int kafkaPartition, long offset, Long previousOffset) {
		msg = new StreamrBinaryMessageV29(streamId, streamPartition, timestamp, ttl, contentType, content, signatureType, address, signature);
		this.offset = offset;
		this.previousOffset = previousOffset;
		this.kafkaPartition = kafkaPartition;
	}

	public StreamrBinaryMessage getStreamrBinaryMessage() {
		return msg;
	}

	public long getOffset() {
		return offset;
	}

	public Long getPreviousOffset() {
		return previousOffset != null && previousOffset >= 0 ? previousOffset : null;
	}

	public int getKafkaPartition() {
		return kafkaPartition;
	}

	public byte[] toBytesWithKafkaMetadata() {
		byte[] orig = msg.toBytes();
		ByteBuffer bb = ByteBuffer.allocate(orig.length + 21);
		bb.put(orig);
		bb.put(VERSION); // 1
		bb.putLong(offset); // 8
		bb.putLong(previousOffset != null ? previousOffset : -1); // 8
		bb.putInt(kafkaPartition); // 4
		return bb.array();
	}


}

