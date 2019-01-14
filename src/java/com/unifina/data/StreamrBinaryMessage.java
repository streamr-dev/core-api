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

public abstract class StreamrBinaryMessage {
	protected static final Charset utf8 = Charset.forName("UTF-8");

	public static final byte CONTENT_TYPE_STRING = 11; //0x0B
	public static final byte CONTENT_TYPE_JSON = 27; //0x1B

	public enum SignatureType {
		SIGNATURE_TYPE_NONE ((byte) 0),
		SIGNATURE_TYPE_ETH ((byte) 1);

		private final byte id;

		SignatureType(byte id) {
			this.id = id;
		}

		public byte getId() {
			return this.id;
		}
	}

	private static Type type = new TypeToken<LinkedHashMap<String, Object>>(){}.getType();

	private static Gson gson = new GsonBuilder()
			.serializeNulls()
			.setDateFormat(DateFormat.LONG)
			.create();

	protected byte version;
	protected String streamId;
	protected int partition;
	protected long timestamp;
	protected byte contentType;
	protected byte[] streamIdAsBytes;
	protected byte[] content;
	protected int ttl;

	public StreamrBinaryMessage(byte version, String streamId, int partition, long timestamp, int ttl, byte contentType, byte[] content) {
		this.version = version;
		this.streamId = streamId;
		this.partition = partition;
		this.streamIdAsBytes = this.streamId.getBytes(utf8);
		this.timestamp = timestamp;
		this.ttl = ttl;
		this.contentType = contentType;
		this.content = content;
	}

	public final byte[] toBytes() {
		ByteBuffer bb;
		bb = ByteBuffer.allocate(sizeInBytes());
		toByteBuffer(bb);
		return bb.array();
	}

	protected void toByteBuffer(ByteBuffer bb) {
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
	}

	public int sizeInBytes() {
		// version + timestamp + ttl + streamId length + streamId + partition + content type + content length + content
		return 1 + 8 + 4 + 1 + streamIdAsBytes.length + 1 + 1 + 4 + content.length;
	}

	public byte getVersion() {
		return version;
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

	public int getPartition() { return partition; }

	public byte getContentType() {
		return contentType;
	}

	public int getTTL() {
		return ttl;
	}

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

	public StreamrMessage toStreamrMessage() {
		return new StreamrMessage(getStreamId(), getPartition(), new Date(getTimestamp()), getContentJSON());
	}
}
