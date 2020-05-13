package com.unifina.utils

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class IDValidator {
	static final Closure validate = { String id ->
		if (id == null) {
			return false
		}
		if (id.length() != 44) {
			return false
		}
		return id ==~ /^[a-zA-Z0-9-_]{44}$/
	}
}
