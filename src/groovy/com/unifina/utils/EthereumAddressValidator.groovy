package com.unifina.utils

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class EthereumAddressValidator {
	static final Closure validate = { String address ->
		if (address == null) {
			return false
		}
		return address.matches("^0x[a-fA-F0-9]{40}\$")
	}
}
