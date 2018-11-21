package com.unifina.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.unifina.feed.StreamrMessage;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashMap;

public class StreamrBinaryMessageV28 extends StreamrBinaryMessage {

	protected static final Charset utf8 = Charset.forName("UTF-8");

	private static Type type = new TypeToken<LinkedHashMap<String, Object>>(){}.getType();

	private static Gson gson = new GsonBuilder()
			.serializeNulls()
			.setDateFormat(DateFormat.LONG)
			.create();

	private final String streamId;
	private final int partition;
	private final long timestamp;
	private final byte contentType;
	private final byte[] streamIdAsBytes;
	private final byte[] content;
	private final int ttl;

	protected StreamrBinaryMessageV28(ByteBuffer bb) {
		timestamp = bb.getLong();
		ttl = bb.getInt();
		int streamIdLength = bb.get() & 0xFF; // unsigned byte
		streamIdAsBytes = new byte[streamIdLength];
		bb.get(streamIdAsBytes);
		streamId = new String(streamIdAsBytes, utf8);
		partition = bb.get() & 0xff; // unsigned byte
		contentType = bb.get();
		int contentLength = bb.getInt();
		content = new byte[contentLength];
		bb.get(content);
	}

	public StreamrBinaryMessageV28(String streamId, int partition, long timestamp, int ttl, byte contentType, byte[] content) {
		this.streamId = streamId;
		this.partition = partition;
		this.streamIdAsBytes = this.streamId.getBytes(utf8);
		this.timestamp = timestamp;
		this.ttl = ttl;
		this.contentType = contentType;
		this.content = content;
	}

	/**
	 * 	version 1 byte
	 * 	timestamp 8 bytes
	 * 	stream id length 1 byte (interpret as unsigned)
	 * 	stream id, N bytes
	 * 	content type 1 byte
	 * 	content length 4 bytes
	 * 	payload, N bytes
	 * 	ttl, 4 bytes
	 */
	@Override
	public byte[] toBytes() {
		ByteBuffer bb;
		bb = ByteBuffer.allocate(sizeInBytes());
		bb.put(getVersion()); // 1 byte
		bb.putLong(timestamp); // 8 bytes
		bb.putInt(ttl); // 4 bytes
		if (streamIdAsBytes.length > 255) {
			throw new IllegalArgumentException("Stream id too long: "+streamId+", length "+streamIdAsBytes.length);
		}
		bb.put((byte) streamIdAsBytes.length); // 1 byte
		bb.put(streamIdAsBytes);
		if (partition > 255) {
			throw new IllegalArgumentException("Partition out of range: "+partition);
		}
		bb.put((byte) partition); // 1 byte
		bb.put(contentType); // 1 byte
		bb.putInt(content.length); // 4 bytes
		bb.put(content); // contentLength bytes
		return bb.array();
	}

	@Override
	public int sizeInBytes() {
		// version + timestamp + ttl + streamId length + streamId + partition + content type + content length + content
		return 1 + 8 + 4 + 1 + streamIdAsBytes.length + 1 + 1 + 4 + content.length;
	}

	@Override
	public byte getVersion() {
		return VERSION;
	}

	@Override
	public String getStreamId() {
		return streamId;
	}

	@Override
	public byte[] getContentBytes() {
		return content;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int getPartition() { return partition; }

	@Override
	public byte getContentType() {
		return contentType;
	}

	@Override
	public int getTTL() {
		return ttl;
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

	protected LinkedHashMap<String, Object> getContentJSON() {
		if (getContentType() == CONTENT_TYPE_JSON) {
			String s = toString();
			return gson.fromJson(s, type);
		}
		throw new RuntimeException("Unknown content type: " + getContentType());
	}

	@Override
	public StreamrMessage toStreamrMessage() {
		return new StreamrMessage(getStreamId(), getPartition(), new Date(getTimestamp()), getContentJSON());
	}
}
