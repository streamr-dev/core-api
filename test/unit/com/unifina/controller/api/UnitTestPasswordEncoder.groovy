package com.unifina.controller.api

import com.unifina.security.PasswordEncoder

class UnitTestPasswordEncoder implements PasswordEncoder {
	@Override
	String encodePassword(String rawPassword) {
		return rawPassword + "-encoded"
	}

	@Override
	boolean isPasswordValid(String encodedPassword, String rawPassword) {
		return encodedPassword == rawPassword + "-encoded"
	}
}
