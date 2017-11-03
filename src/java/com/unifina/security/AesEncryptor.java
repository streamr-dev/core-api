package com.unifina.security;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public class AesEncryptor {
	private final TextEncryptor textEncryptor;

	public AesEncryptor(String password, String salt) {
		textEncryptor = Encryptors.text(password, salt);
	}

	public String encrypt(String plaintext) {
		return textEncryptor.encrypt(plaintext);
	}

	public String decrypt(String encrypted) {
		return textEncryptor.decrypt(encrypted);
	}
}
