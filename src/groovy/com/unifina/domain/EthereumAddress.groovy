package com.unifina.domain

import com.unifina.service.ValidationException
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class EthereumAddress {
	private String value

	EthereumAddress(String value) {
		if (!EthereumAddressValidator.validate.call(value)) {
			throw new ValidationException("Address is not a valid Ethereum address")
		}
		this.value = value.toLowerCase()
	}

	public String toString() {
		return value
	}
}