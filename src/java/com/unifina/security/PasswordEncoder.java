package com.unifina.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public interface PasswordEncoder {

	String encodePassword(String rawPassword);

	boolean isPasswordValid(String encodedPassword, String rawPassword);
}
