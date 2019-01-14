package com.unifina.data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class StreamrBinaryMessageFactory {
	private static final Charset utf8 = Charset.forName("UTF-8");

	public static StreamrBinaryMessage fromBytes(ByteBuffer bb) {
		byte version = bb.get();
		if (version == StreamrBinaryMessageV28.VERSION) {
			return fromBytesToV28(bb);
		} else if (version == StreamrBinaryMessageV29.VERSION) {
			return fromBytesToV29(bb);
		} else if (version == StreamrBinaryMessageV30.VERSION) {
			return fromBytesToV30(bb);
		} else {
			throw new IllegalArgumentException("Unknown version byte: "+version);
		}
	}

	private static StreamrBinaryMessageV28 fromBytesToV28(ByteBuffer bb) {
		long timestamp = bb.getLong();
		int ttl = bb.getInt();
		int streamIdLength = bb.get() & 0xFF; // unsigned byte
		byte[] streamIdAsBytes = new byte[streamIdLength];
		bb.get(streamIdAsBytes);
		String streamId = new String(streamIdAsBytes, utf8);
		int partition = bb.get() & 0xff; // unsigned byte
		byte contentType = bb.get();
		int contentLength = bb.getInt();
		byte[] content = new byte[contentLength];
		bb.get(content);
		return new StreamrBinaryMessageV28(streamId, partition, timestamp, ttl, contentType, content);
	}

	private static StreamrBinaryMessageV29 fromBytesToV29(ByteBuffer bb) {
		long timestamp = bb.getLong();
		int ttl = bb.getInt();
		int streamIdLength = bb.get() & 0xFF; // unsigned byte
		byte[] streamIdAsBytes = new byte[streamIdLength];
		bb.get(streamIdAsBytes);
		String streamId = new String(streamIdAsBytes, utf8);
		int partition = bb.get() & 0xff; // unsigned byte
		byte contentType = bb.get();
		int contentLength = bb.getInt();
		byte[] content = new byte[contentLength];
		bb.get(content);
		byte signatureTypeByte = bb.get();
		StreamrBinaryMessage.SignatureType signatureType;
		byte[] addressBytes;
		byte[] signatureBytes;
		if (signatureTypeByte == StreamrBinaryMessage.SignatureType.SIGNATURE_TYPE_ETH.getId()) {
			signatureType = StreamrBinaryMessage.SignatureType.SIGNATURE_TYPE_ETH;
			addressBytes = new byte[20];
			bb.get(addressBytes);
			signatureBytes = new byte[65];
			bb.get(signatureBytes);
		} else if (signatureTypeByte == StreamrBinaryMessage.SignatureType.SIGNATURE_TYPE_NONE.getId()) {
			signatureType = StreamrBinaryMessage.SignatureType.SIGNATURE_TYPE_NONE;
			addressBytes = null;
			signatureBytes = null;
		} else {
			throw new IllegalArgumentException("Unknown signature type: "+signatureTypeByte);
		}
		return new StreamrBinaryMessageV29(streamId, partition, timestamp, ttl, contentType, content,
				signatureType, addressBytes, signatureBytes);
	}

	private static StreamrBinaryMessageV30 fromBytesToV30(ByteBuffer bb) {
		int streamIdLength = bb.get() & 0xFF; // unsigned byte
		byte[] streamIdAsBytes = new byte[streamIdLength];
		bb.get(streamIdAsBytes);
		String streamId = new String(streamIdAsBytes, utf8);
		int partition = bb.get() & 0xff; // unsigned byte
		long timestamp = bb.getLong();
		int sequenceNumber = bb.getInt();
		int publisherIdLength = bb.get() & 0xFF; // unsigned byte;
		byte[] publisherIdBytes = new byte[publisherIdLength];
		bb.get(publisherIdBytes);
		long prevTimestamp = bb.getLong();
		int prevSequenceNumber = bb.getInt();
		int ttl = bb.getInt();
		byte contentType = bb.get();
		int contentLength = bb.getInt();
		byte[] content = new byte[contentLength];
		bb.get(content);
		byte signatureTypeByte = bb.get();
		StreamrBinaryMessage.SignatureType signatureType;
		byte[] signatureBytes;
		if (signatureTypeByte == StreamrBinaryMessage.SignatureType.SIGNATURE_TYPE_ETH.getId()) {
			signatureType = StreamrBinaryMessage.SignatureType.SIGNATURE_TYPE_ETH;
			signatureBytes = new byte[65];
			bb.get(signatureBytes);
		} else if (signatureTypeByte == StreamrBinaryMessage.SignatureType.SIGNATURE_TYPE_NONE.getId()) {
			signatureType = StreamrBinaryMessage.SignatureType.SIGNATURE_TYPE_NONE;
			signatureBytes = null;
		} else {
			throw new IllegalArgumentException("Unknown signature type: "+signatureTypeByte);
		}
		return new StreamrBinaryMessageV30(streamId, partition, timestamp, sequenceNumber, publisherIdBytes,
				prevTimestamp, prevSequenceNumber, ttl, contentType, content, signatureType, signatureBytes);
	}
}
