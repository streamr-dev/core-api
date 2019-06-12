package com.unifina.utils

/*
This is a temporary workaround because right now users can be defined with an email address or an ethereum address.
 */
class UsernameValidator {
	static validate = { String username ->
		return EmailValidator.validate || EthereumAddressValidator.validate
	}
}
