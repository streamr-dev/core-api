package com.unifina.utils

import grails.compiler.GrailsCompileStatic

/*
This is a temporary workaround because right now users can be defined with an email address or an ethereum address.
 */
@GrailsCompileStatic
class UsernameValidator {
	static final Closure validate = { String username ->
		boolean isEmailValid = EmailValidator.validate.call(username)
		boolean isEthereumAddressValid = EthereumAddressValidator.validate.call(username)
		boolean result = isEmailValid || isEthereumAddressValid
		return result
	}

	static final Closure validateUsernameOrNull = { String username ->
		if (username == null) {
			return true
		}
		boolean result = UsernameValidator.validate.call(username)
		return result
	}
}
