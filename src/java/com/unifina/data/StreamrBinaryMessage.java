package com.unifina.data;

import com.unifina.feed.StreamrMessage;

import java.nio.ByteBuffer;

public abstract class StreamrBinaryMessage {

	public static final byte CONTENT_TYPE_STRING = 11; //0x0B
	public static final byte CONTENT_TYPE_JSON = 27; //0x1B

	protected static final byte VERSION = 28; //0x1C
	protected static final byte VERSION_SIGNED = 29; //0x1D

	public static StreamrBinaryMessage from(ByteBuffer bb) {
		byte version = bb.get();
		if (version == VERSION) {
			return new StreamrBinaryMessageV28(bb);
		} else if (version == VERSION_SIGNED) {
			return new StreamrBinaryMessageV29(bb);
		} else {
			throw new IllegalArgumentException("Unknown version byte: "+version);
		}
	}

	protected abstract void toByteBuffer(ByteBuffer bb);

	public final byte[] toBytes() {
		ByteBuffer bb;
		bb = ByteBuffer.allocate(sizeInBytes());
		toByteBuffer(bb);
		return bb.array();
	}

	public abstract int sizeInBytes();

	public abstract byte getVersion();

	public abstract String getStreamId();

	public abstract byte[] getContentBytes();

	public abstract long getTimestamp();

	public abstract int getPartition();

	public abstract byte getContentType();

	public abstract int getTTL();

	public abstract StreamrMessage toStreamrMessage();
}
