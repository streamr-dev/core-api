package com.unifina.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.xml.bind.DatatypeConverter;

public class StreamrBinaryMessage {
	private static final byte VERSION = 28; //0x1C
	private static final byte VERSION_SIGNED = 29; //0x1D

	public enum SignatureType {
		SIGNATURE_TYPE_NONE ((byte) 0),
		SIGNATURE_TYPE_ETH ((byte) 1);

		private final byte id;

		private SignatureType(byte id) {
			this.id = id;
		}

		public byte getId() {
			return this.id;
		}
	}

	public static final byte CONTENT_TYPE_STRING = 11; //0x0B
	public static final byte CONTENT_TYPE_JSON = 27; //0x1B

	private static final Charset utf8 = Charset.forName("UTF-8");

	private final byte version;
	private final String streamId;
	private final int partition;
	private final long timestamp;
	private final byte contentType;
	private final byte[] streamIdAsBytes;
	private final byte[] content;
	private final int ttl;
	private final SignatureType signatureType;
	private final byte[] addressBytes;
	private final byte[] signatureBytes;

	public StreamrBinaryMessage(ByteBuffer bb) {
		version = bb.get();

		// If the message starts with a version byte, parse timestamp and contentType headers
		if (version == VERSION || version == VERSION_SIGNED) {
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
			if (version == VERSION_SIGNED) {
				byte signatureTypeByte = bb.get();
				if (signatureTypeByte == SignatureType.SIGNATURE_TYPE_ETH.getId()) {
					signatureType = SignatureType.SIGNATURE_TYPE_ETH;
					addressBytes = new byte[20];
					bb.get(addressBytes);
					signatureBytes = new byte[65];
					bb.get(signatureBytes);
				} else if (signatureTypeByte == SignatureType.SIGNATURE_TYPE_NONE.getId()) {
					signatureType = SignatureType.SIGNATURE_TYPE_NONE;
					addressBytes = null;
					signatureBytes = null;
				} else {
					throw new IllegalArgumentException("Unknown signature type: "+signatureTypeByte);
				}
			} else {
				signatureType = SignatureType.SIGNATURE_TYPE_NONE;
				addressBytes = null;
				signatureBytes = null;
			}
		}
		else {
			throw new IllegalArgumentException("Unknown version byte: "+version);
		}
	}

	public StreamrBinaryMessage(String streamId, int partition, long timestamp, int ttl, byte contentType, byte[] content) {
		this.version = VERSION;
		this.streamId = streamId;
		this.partition = partition;
		this.streamIdAsBytes = this.streamId.getBytes(utf8);
		this.timestamp = timestamp;
		this.ttl = ttl;
		this.contentType = contentType;
		this.content = content;
		this.signatureType = SignatureType.SIGNATURE_TYPE_NONE;
		this.addressBytes = null;
		this.signatureBytes = null;
	}

	public StreamrBinaryMessage(byte version, String streamId, int partition, long timestamp, int ttl, byte contentType, byte[] content,
								SignatureType signatureType, String address, String signature) {
		this.version = version;
		this.streamId = streamId;
		this.partition = partition;
		this.streamIdAsBytes = this.streamId.getBytes(utf8);
		this.timestamp = timestamp;
		this.ttl = ttl;
		this.contentType = contentType;
		this.content = content;
		this.signatureType = signatureType;
		this.addressBytes = hexToBytes(address);
		this.signatureBytes = hexToBytes(signature);
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
	public byte[] toBytes() {
		ByteBuffer bb;
		bb = ByteBuffer.allocate(sizeInBytes());
		bb.put(version); // 1 byte
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
		if (version == VERSION_SIGNED) {
			bb.put(signatureType.getId()); // 1 byte
			if (signatureType == SignatureType.SIGNATURE_TYPE_ETH) {
				bb.put(addressBytes); // 20 bytes
				bb.put(signatureBytes); // 65 bytes
			}
		}
		return bb.array();
	}

	public int sizeInBytes() {
		// version + timestamp + ttl + streamId length + streamId + partition + content type + content length + content
		int v28Size = 1 + 8 + 4 + 1 + streamIdAsBytes.length + 1 + 1 + 4 + content.length;
		if (version == VERSION) {
			return v28Size;
		} else if (version == VERSION_SIGNED) {
			if (signatureType == SignatureType.SIGNATURE_TYPE_NONE) {
				return v28Size + 1;
			} else if (signatureType == SignatureType.SIGNATURE_TYPE_ETH) {
				return v28Size + 1 + 20 + 65;
			} else {
				throw new IllegalArgumentException("Unknown signature type: "+signatureType);
			}
		} else {
			throw new IllegalArgumentException("Unknown version byte: "+version);
		}
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

	public SignatureType getSignatureType() {
		return signatureType;
	}

	public String getAddress() {
		return bytesToHex(addressBytes);
	}

	public String getSignature() {
		return bytesToHex(signatureBytes);
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

	private byte[] hexToBytes(String s) {
		if (s == null) {
			return null;
		}
		if (s.startsWith("0x")) {
			return DatatypeConverter.parseHexBinary(s.substring(2));
		}
		return DatatypeConverter.parseHexBinary(s);
	}

	private String bytesToHex(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		return "0x" + DatatypeConverter.printHexBinary(bytes);
	}

}
