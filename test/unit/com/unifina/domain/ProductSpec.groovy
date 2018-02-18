package com.unifina.domain

import com.unifina.domain.marketplace.Product
import spock.lang.Specification
import spock.lang.Unroll

class ProductSpec extends Specification {
	@Unroll
	void "isEthereumAddress(#value) == #expected"(String value, Object expected) {
		expect:
		Product.isEthereumAddress(value, null) == expected
		where:
		value                                        | expected
		null                                         | "validation.isEthereumAddress"
		""                                           | "validation.isEthereumAddress"
		"0x0"                                        | "validation.isEthereumAddress"
		"0xfffFFffFfffFffffFFFFffffFFFFfffFFFFfffFf" | true
		"0x0000000000000000000000000000000000000000" | true
		"0x0123456789abcdefABCDEF000000000000000000" | true
		"1x0000000000000000000000000000000000000000" | "validation.isEthereumAddress"
		"0xG000000000000000000000000000000000000000" | "validation.isEthereumAddress"
		"0x000000000000000000000000000000000000000"  | "validation.isEthereumAddress"
	}

	@Unroll
	void "isEthereumTransaction(#value) == #expected"(String value, Object expected) {
		expect:
		Product.isEthereumTransaction(value, null) == expected
		where:
		value                                                                | expected
		null                                                                 | true
		""                                                                   | "validation.isEthereumTransaction"
		"0x0"                                                                | "validation.isEthereumTransaction"
		"0xfffFFffFfffFffffFFFFffffFFFFfffFFFFfffFfFffffFFFFfFffFFFfffFfFfF" | true
		"0x0000000000000000000000000000000000000000000000000000000000000000" | true
		"0x0123456789abcdefABCDEF000000000000000000000000000000000000000000" | true
		"1x0000000000000000000000000000000000000000000000000000000000000000" | "validation.isEthereumTransaction"
		"0xG000000000000000000000000000000000000000000000000000000000000000" | "validation.isEthereumTransaction"
		"0x000000000000000000000000000000000000000000000000000000000000000"  | "validation.isEthereumTransaction"
	}
}
