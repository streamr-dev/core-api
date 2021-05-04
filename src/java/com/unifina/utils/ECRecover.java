package com.unifina.utils;

import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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

	public static String recoverAddress(byte[] messageHash, String signatureHex) {
		final ECDSASignature signature;
		byte[] source = Numeric.hexStringToByteArray(signatureHex);
		BigInteger r = toBigInteger(source, 0, 32);
		BigInteger s = toBigInteger(source, 32, 64);
		signature = new ECDSASignature(r, s);
		for (byte i = 0; i < 4; i++) {
			BigInteger publicKey;
			try {
				publicKey = Sign.recoverFromSignature(i, signature, messageHash);
			} catch (RuntimeException e) {
				continue;
			}
			if (publicKey != null) {
				String address = Keys.getAddress(publicKey);
				String addr = Numeric.prependHexPrefix(address);
				return addr;
			}
		}
		return null;
	}

	private static BigInteger toBigInteger(final byte[] source, final int from, final int to) {
		byte[] bytes = Arrays.copyOfRange(source, from, to);
		return new BigInteger(1, bytes);
	}
}
