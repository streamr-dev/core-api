package com.unifina.feed.redis;

import com.unifina.data.StreamrBinaryMessage;

import java.nio.ByteBuffer;

/**
 * Extends StreamrBinaryMessage by appending Kafka offset and partition information.
 * Suffixes the StreamrBinaryMessage binary with
 * - version 1 byte
 * - offset 8 bytes (long)
 * - partition 4 bytes (int)
 */
public class StreamrBinaryMessageFromRedis extends StreamrBinaryMessage {

	private static final byte VERSION = 0;

	private final long offset;
	private final int partition;

	public StreamrBinaryMessageFromRedis(ByteBuffer bb) {
		super(bb);

		byte version = bb.get();
		if (version == 0) {
			offset = bb.getLong();
			partition = bb.getInt();
		}
		else throw new IllegalArgumentException("Invalid version: "+version);
	}

	public StreamrBinaryMessageFromRedis(String streamId, long timestamp, byte contentType, byte[] content, long offset, int partition) {
		super(streamId, timestamp, contentType, content);
		this.offset = offset;
		this.partition = partition;
	}

	public long getOffset() {
		return offset;
	}

	public int getPartition() {
		return partition;
	}

	@Override
	public byte[] toBytes() {
		byte[] orig = super.toBytes();
		ByteBuffer bb = ByteBuffer.allocate(orig.length + 13);
		bb.put(orig);
		bb.put(VERSION);
		bb.putLong(offset);
		bb.putInt(partition);
		return bb.array();
	}
}
