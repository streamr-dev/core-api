package com.unifina.crypto;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;

import java.security.SignatureException;

public class ECRecover {
	private static final String SIGN_MAGIC = "\u0019Ethereum Signed Message:\n";

	public static byte[] calculateMessageHash(String message) throws DecoderException {
		String messageHex = "0x" + Hex.encodeHexString(message.getBytes());
		byte[] messageBytes = Hex.decodeHex(messageHex.replace("0x", "").toCharArray());
		String prefix = SIGN_MAGIC + messageBytes.length;
		byte[] toHash = ByteUtil.merge(prefix.getBytes(), messageBytes);
		return HashUtil.sha3(toHash);
	}

	public static String recoverAddress(byte[] messageHash, String signatureHex) throws SignatureException, DecoderException {
		byte[] signature = Hex.decodeHex(signatureHex.replace("0x", "").toCharArray());

		byte[] r = new byte[32];
		byte[] s = new byte[32];
		byte v = signature[64];
		System.arraycopy(signature, 0, r, 0, r.length);
		System.arraycopy(signature, 32, s, 0, s.length);

		ECKey.ECDSASignature signatureObj = ECKey.ECDSASignature.fromComponents(r, s, v);
		return "0x" + Hex.encodeHexString(ECKey.signatureToKey(messageHash, signatureObj.toBase64()).getAddress());
	}
}
