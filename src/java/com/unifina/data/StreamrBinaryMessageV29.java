package com.unifina.data;

import com.unifina.feed.StreamrMessage;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Date;

public class StreamrBinaryMessageV29 extends StreamrBinaryMessage {
	public static final byte VERSION = 29; //0x1D

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

	private final SignatureType signatureType;
	private final byte[] addressBytes;
	private final byte[] signatureBytes;

	public StreamrBinaryMessageV29(String streamId, int partition, long timestamp, int ttl, byte contentType, byte[] content,
								SignatureType signatureType, String address, String signature) {
		super(VERSION, streamId, partition, timestamp, ttl, contentType, content);
		this.signatureType = signatureType;
		this.addressBytes = hexToBytes(address);
		this.signatureBytes = hexToBytes(signature);
	}

	public StreamrBinaryMessageV29(String streamId, int partition, long timestamp, int ttl, byte contentType, byte[] content,
								   SignatureType signatureType, byte[] addressBytes, byte[] signatureBytes) {
		super(VERSION, streamId, partition, timestamp, ttl, contentType, content);
		this.signatureType = signatureType;
		this.addressBytes = addressBytes;
		this.signatureBytes = signatureBytes;
	}

	@Override
	protected void toByteBuffer(ByteBuffer bb) {
		super.toByteBuffer(bb);
		bb.put(signatureType.getId()); // 1 byte
		if (signatureType == SignatureType.SIGNATURE_TYPE_ETH) {
			bb.put(addressBytes); // 20 bytes
			bb.put(signatureBytes); // 65 bytes
		}
	}

	@Override
	public int sizeInBytes() {
		if (signatureType == SignatureType.SIGNATURE_TYPE_NONE) {
			// super + signatureType
			return super.sizeInBytes() + 1;
		} else if (signatureType == SignatureType.SIGNATURE_TYPE_ETH) {
			// super + signatureType + addressBytes + signatureBytes
			return super.sizeInBytes() + 1 + 20 + 65;
		} else {
			throw new IllegalArgumentException("Unknown signature type: "+signatureType);
		}
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

	@Override
	public StreamrMessage toStreamrMessage() {
		return new StreamrMessage(getStreamId(), getPartition(), new Date(getTimestamp()), getContentJSON(), getSignatureType(), getAddress(), getSignature());
	}
}
