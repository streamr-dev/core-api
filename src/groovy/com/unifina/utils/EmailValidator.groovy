package com.unifina.utils

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class EmailValidator {
	public static final String EMAIL_REGEX = ".+@.+\\..+"

	static final Closure validate = { String email ->
		if (email == null) {
			return false
		}
		return email.matches(EMAIL_REGEX)
	}

	static final Closure validateNullEmail = { String email ->
		if (email == null) {
			return true
		}
		return email.matches(EMAIL_REGEX)
	}
}
