package com.unifina.utils

class EmailValidator {
	public static final String EMAIL_REGEX = ".+@.+\\..+"

	static validate = { String email ->
		if (email == null) {
			return false
		}
		return email.matches(EMAIL_REGEX)
	}

	static validateNullEmail = { String email ->
		if (email == null) {
			return true
		}
		return email.matches(EMAIL_REGEX)
	}
}
