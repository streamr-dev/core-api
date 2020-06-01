package com.unifina.security;

public class BCryptPasswordEncoder implements PasswordEncoder {
	private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder bcrypt;

	public BCryptPasswordEncoder(final int logRounds) {
		bcrypt = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(logRounds);
	}

	public String encodePassword(String rawPass) {
		return bcrypt.encode(rawPass);
	}

	public boolean isPasswordValid(String encPass, String rawPass) {
		return bcrypt.matches(rawPass, encPass);
	}
}
