package com.unifina.domain

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class CustomStreamIDValidator {

	public static final String SANDBOX_PREFIX = "sandbox"
	// path rules:
	// - must start by slash
	// - can contain chars a-z, A-Z, 0-9 and -_.
	// - must not end with non-word character (in this case - or .)
	// - can have segments separated by slashes (two consecutive slashes is not allowed)
	public static final String PATH_REGEX = "^/(?:[\\w\\.-]+/?)*\\w\$"

	static final Closure validate = { String id ->
		if (id == null) {
			return true
		} else {
			if (id.startsWith(CustomStreamIDValidator.SANDBOX_PREFIX)) {
				String path = id.substring(SANDBOX_PREFIX.length())
				return path.matches(PATH_REGEX)
			} else {
				return false;
			}
		}
	}
}
