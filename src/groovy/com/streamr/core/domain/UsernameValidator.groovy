package com.streamr.core.domain

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class UsernameValidator {
	public static final Closure validate = { String username ->
		boolean isEthereumAddressValid = EthereumAddressValidator.validate.call(username)
		return isEthereumAddressValid
	}

	public static final Closure validateUsernameOrNull = { String username ->
		if (username == null) {
			return true
		}
		boolean result = UsernameValidator.validate.call(username)
		return result
	}
}
