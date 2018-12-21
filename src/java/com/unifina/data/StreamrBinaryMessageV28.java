package com.unifina.data;

public class StreamrBinaryMessageV28 extends StreamrBinaryMessage {
	public static final byte VERSION = 28; //0x1C

	public StreamrBinaryMessageV28(String streamId, int partition, long timestamp, int ttl, byte contentType, byte[] content) {
		super(VERSION, streamId, partition, timestamp, ttl, contentType, content);
	}
}
