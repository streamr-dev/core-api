package com.unifina.utils

class IDValidator {
	static validate = { String id ->
		if (id == null) {
			return false
		}
		if (id.length() != 44) {
			return false
		}
		return id ==~ /^[a-zA-Z0-9-_]{44}$/
	}
}
