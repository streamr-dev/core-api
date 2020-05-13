package com.unifina.utils

/*
This is a temporary workaround because right now users can be defined with an email address or an ethereum address.
 */
class UsernameValidator {
	static validate = { String username ->
		boolean isEmailValid = EmailValidator.validate(username)
		boolean isEthereumAddressValid = EthereumAddressValidator.validate(username)
		boolean result = isEmailValid || isEthereumAddressValid
		return result
	}

	static validateUsernameOrNull = { String username ->
		if (username == null) {
			return true
		}
		boolean result = UsernameValidator.validate(username)
		return result
	}
}
