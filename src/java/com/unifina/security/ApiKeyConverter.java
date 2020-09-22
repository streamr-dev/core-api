package com.unifina.security;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.util.EncodingUtils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;

public class ApiKeyConverter {

	private static final byte[] SALT = new byte[] {0};
	private static final int ITERATION_COUNT = 4096;
	private static final int KEY_LENGTH = 256;

	public static String createEthereumPrivateKey(String apiKey) {
		try {
			PBEKeySpec spec = new PBEKeySpec(apiKey.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512.name());
			return String.valueOf(Hex.encode(skf.generateSecret(spec).getEncoded()));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Could not create hash", e);
		}
	}
}
