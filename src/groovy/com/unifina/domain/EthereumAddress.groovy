package com.unifina.domain

import org.web3j.crypto.Keys
import com.unifina.service.ValidationException
import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode

@GrailsCompileStatic
@EqualsAndHashCode
class EthereumAddress {
	private String value

	EthereumAddress(String value) {
		if (!EthereumAddressValidator.validate.call(value)) {
			throw new ValidationException("Address is not a valid Ethereum address")
		}
		this.value = Keys.toChecksumAddress(value)
	}

	public String toString() {
		return value
	}
}