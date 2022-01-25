package com.streamr.core.domain
import com.streamr.core.domain.EthereumAddressValidator
import spock.lang.Specification
import spock.lang.Unroll

class EthereumAddressValidatorSpec extends Specification {
	@Unroll
	void "validate(#value) == #expected"(String value, Boolean expected) {
		expect:
        EthereumAddressValidator.validate(value) == expected
		where:
		value                                        | expected
		null                                         | false
		""                                           | false
		"0x0"                                        | false
		"0xfffFFffFfffFffffFFFFffffFFFFfffFFFFfffFf" | true
		"0x0000000000000000000000000000000000000000" | true
		"0x0123456789abcdefABCDEF000000000000000000" | true
		"1x0000000000000000000000000000000000000000" | false
		"0xG000000000000000000000000000000000000000" | false
		"0x000000000000000000000000000000000000000"  | false
	}

	void "isNullOrValid(null) == true"() {
		expect:
		EthereumAddressValidator.isNullOrValid(null)
	}
}
