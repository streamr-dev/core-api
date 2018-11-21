package com.unifina.data;

import com.unifina.feed.StreamrMessage;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Date;

public class StreamrBinaryMessageV29 extends StreamrBinaryMessageV28 {

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

	protected StreamrBinaryMessageV29(ByteBuffer bb) {
		super(bb);
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
	}

	public StreamrBinaryMessageV29(String streamId, int partition, long timestamp, int ttl, byte contentType, byte[] content,
								SignatureType signatureType, String address, String signature) {
		super(streamId, partition, timestamp, ttl, contentType, content);
		this.signatureType = signatureType;
		this.addressBytes = hexToBytes(address);
		this.signatureBytes = hexToBytes(signature);
	}

	@Override
	public byte[] toBytes() {
		ByteBuffer bb;
		bb = ByteBuffer.allocate(sizeInBytes());
		// When calling super.toBytes(), the sizeInBytes() method called is the one in this child class
		// and not the one in the parent class... As a result the byte array is too large and needs to be truncated.
		byte[] v28 = java.util.Arrays.copyOf(super.toBytes(), super.sizeInBytes());
		bb.put(v28);
		bb.put(signatureType.getId()); // 1 byte
		if (signatureType == SignatureType.SIGNATURE_TYPE_ETH) {
			bb.put(addressBytes); // 20 bytes
			bb.put(signatureBytes); // 65 bytes
		}
		return bb.array();
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

	@Override
	public byte getVersion() {
		return VERSION_SIGNED;
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
