package com.unifina.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class EncodedMessage {
	private static final byte VERSION = 28; //0x1C

	public static final byte CONTENT_TYPE_STRING = 11; //0x0B
	public static final byte CONTENT_TYPE_JSON = 27; //0x1B

	private static final Charset utf8 = Charset.forName("UTF-8");

	private final String streamId;
	private final long timestamp;
	private final byte contentType;
	private final byte[] streamIdAsBytes;
	private final byte[] content;

	public EncodedMessage(String streamId, long timestamp, byte contentType, byte[] content) {
		this.streamId = streamId;
		this.streamIdAsBytes = this.streamId.getBytes(utf8);
		this.timestamp = timestamp;
		this.contentType = contentType;
		this.content = content;
	}

	/**
	 * 	version 1 byte
	 * 	timestamp 8 bytes
	 * 	stream id length 1 byte (interpret as unsigned)
	 * 	stream id, N bytes
	 * 	content type 1 byte
	 * 	payload, the rest of the msg
	 */
	public byte[] toBytes() {
		ByteBuffer bb;
		bb = ByteBuffer.allocate(11+streamIdAsBytes.length+content.length); // version + timestamp + stream id length + stream id + content type + content length
		bb.put(VERSION); // 1 byte
		bb.putLong(timestamp); // 8 bytes
		if (streamIdAsBytes.length > 255) {
			throw new IllegalArgumentException("Stream id too long: "+streamId+", length "+streamIdAsBytes.length);
		}
		bb.put((byte) streamIdAsBytes.length);
		bb.put(streamIdAsBytes);
		bb.put(contentType); // 1 byte
		bb.put(content);
		return bb.array();
	}

	public String getStreamId() {
		return streamId;
	}

	public byte[] getContentBytes() {
		return content;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public byte getContentType() {
		return contentType;
	}

	@Override
	public String toString() {
		if (contentType==CONTENT_TYPE_STRING || contentType==CONTENT_TYPE_JSON)
			try {
				return new String(getContentBytes(),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Platform does not support UTF-8!");
			}
		else return super.toString();
	}
}
