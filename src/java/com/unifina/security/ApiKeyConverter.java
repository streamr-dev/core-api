package com.unifina.security;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.util.EncodingUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;

public class ApiKeyConverter {

	private static final int HEX_DIGIT_BIT_COUNT = 4;
	private static final byte[] SALT = new byte[] {0};
	private static final int ETHEREUM_PRIVATE_KEY_LENGTH = 64;
	public static final int ITERATION_COUNT = 4096;

	public static String createEthereumPrivateKey(String apiKey) {
		try {
			int keyLength = (ETHEREUM_PRIVATE_KEY_LENGTH - Hex.encode(SALT).length) * HEX_DIGIT_BIT_COUNT;
			PBEKeySpec spec = new PBEKeySpec(apiKey.toCharArray(), SALT, ITERATION_COUNT, keyLength);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA1.name());
			return String.valueOf(Hex.encode(EncodingUtils.concatenate(new byte[][]{SALT, skf.generateSecret(spec).getEncoded()})));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Could not create hash", e);
		}
	}
}
