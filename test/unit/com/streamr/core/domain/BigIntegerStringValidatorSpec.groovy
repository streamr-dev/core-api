package com.streamr.core.domain


import spock.lang.Specification
import spock.lang.Unroll

class BigIntegerStringValidatorSpec extends Specification {
	@Unroll
	void "validateNonNegative(#value) == #expected"() {
		expect:
		BigIntegerStringValidator.validateNonNegative(value) == expected
		where:
		value                                        | expected
		null                                         | false
		""                                           | false
		"-1"                                         | false
		"0"                                          | true
		"1234567890123456789012345678901234567890"   | true
	}

	@Unroll
	void "isNullOrNonNegative(#value) == #expected"() {
		expect:
		BigIntegerStringValidator.isNullOrNonNegative(value) == expected
		where:
		value                                        | expected
		null                                         | true
		""                                           | false
		"-1"                                         | false
		"0"                                          | true
		"1234567890123456789012345678901234567890"   | true
	}
}
