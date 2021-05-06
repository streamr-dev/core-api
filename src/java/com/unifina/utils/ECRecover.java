package com.unifina.utils;

import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.Arrays;

public final class ECRecover {
	private static final String SIGN_MAGIC = "\u0019Ethereum Signed Message:\n";

	private ECRecover() {
	}

	public static byte[] calculateMessageHash(String message) {
		int msgLen = message.getBytes(StandardCharsets.UTF_8).length;
		String s = String.format("%s%d%s", SIGN_MAGIC, msgLen, message);
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		return Hash.sha3(bytes);
	}

	public static String recoverAddress(byte[] messageHash, String signatureHex) throws SignatureException {
		byte[] source = Numeric.hexStringToByteArray(signatureHex);
		byte v = source[64];
		if (v < 27) {
			v += 27;
		}
		byte[] r = Arrays.copyOfRange(source, 0, 32);
		byte[] s = Arrays.copyOfRange(source, 32, 64);
		Sign.SignatureData signature = new Sign.SignatureData(v, r, s);
		for (byte i = 0; i < 4; i++) {
			BigInteger publicKey;
			try {
				publicKey = Sign.signedMessageHashToKey(messageHash, signature);
			} catch (SignatureException e) {
				continue;
			}
			if (publicKey != null) {
				String address = Keys.getAddress(publicKey);
				return Numeric.prependHexPrefix(address);
			}
		}
		throw new SignatureException("Address recovery from signature failed.");
	}
}
