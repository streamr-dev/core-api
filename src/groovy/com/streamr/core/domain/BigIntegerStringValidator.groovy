package com.streamr.core.domain

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class BigIntegerStringValidator {
	public static final Closure validateNonNegative = { String bigInteger ->
		try {
			BigInteger number = new BigInteger(bigInteger) // throws if not valid
			return number.compareTo(0) >= 0
		} catch (err) {
			return false
		}
	}

	public static final Closure isNullOrNonNegative = { String bigInteger ->
		if (bigInteger == null) {
			return true
		}
		try {
			BigInteger number = new BigInteger(bigInteger) // throws if not valid
			return number.compareTo(0) >= 0
		} catch (err) {
			return false
		}
	}
}
