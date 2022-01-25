package com.streamr.core.domain

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class EthereumAddressValidator {
	private final static String REGEX = "^0x[a-fA-F0-9]{40}\$"

	public static final Closure validate = { String address ->
		if (address == null) {
			return false
		}
		return address.matches(REGEX)
	}

	public static final Closure isNullOrValid = { String address ->
		if (address == null) {
			return true
		}
		return address.matches(REGEX)
	}
}
