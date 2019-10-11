package com.unifina.security;

import org.apache.commons.codec.binary.Hex;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * Helper class to encrypt and decrypt strings using a symmetric key and a salt.
 * Uses AES with PBKDF2WithHmacSHA1 for 256-bit key derivation with helper
 * classes from org.springframework.security.crypto and javax.crypto.
 */
public class StringEncryptor {
	private final String password;

	public StringEncryptor(String password) {
		this.password = password;
	}

	public String encrypt(String plaintext, byte[] salt) {
		return encoder(salt).encrypt(plaintext);
	}

	public String decrypt(String encrypted, byte[] salt) {
		return encoder(salt).decrypt(encrypted);
	}

	private TextEncryptor encoder(byte[] salt) {
		return Encryptors.text(password, Hex.encodeHexString(salt));
	}
}
