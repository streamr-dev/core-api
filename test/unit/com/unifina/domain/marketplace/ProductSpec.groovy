package com.unifina.domain.marketplace

import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Product)
@Mock(SecUser)
class ProductSpec extends Specification {
	@Unroll
	void "isEthereumAddress(#value) == #expected"(String value, Object expected) {
		expect:
		Product.isEthereumAddress(value) == expected
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

	void "isEthereumAddressOrIsNull(null) == true"() {
		expect:
		Product.isEthereumAddressOrIsNull(null)
	}

	void "previewStream() validator passes if previewStream = null and streams empty"() {
		def p = new Product(
				name: "name",
				description: "description",
				ownerAddress: "0x0000000000000000000000000000000000000000",
				beneficiaryAddress: "0x0000000000000000000000000000000000000000",
				pricePerSecond: 1,
				category: new Category(),
				owner: Mock(SecUser)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 0
	}

	void "previewStream() validator does not pass if previewStream != null and streams empty"() {
		def s1 = new Stream(name: "stream-1")
		s1.id = "1"

		def p = new Product(
				name: "name",
				description: "description",
				ownerAddress: "0x0000000000000000000000000000000000000000",
				beneficiaryAddress: "0x0000000000000000000000000000000000000000",
				pricePerSecond: 1,
				category: new Category(),
				previewStream: s1,
				owner: Mock(SecUser)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 1
		p.errors.fieldErrors.get(0).field == "previewStream"
	}

	void "previewStream() validator does not pass if previewStream not included in streams"() {
		def s1 = new Stream(name: "stream-1")
		def s2 = new Stream(name: "stream-2")
		def s3 = new Stream(name: "stream-3")
		s1.id = "1"
		s2.id = "2"
		s3.id = "3"

		def p = new Product(
				name: "name",
				description: "description",
				ownerAddress: "0x0000000000000000000000000000000000000000",
				beneficiaryAddress: "0x0000000000000000000000000000000000000000",
				pricePerSecond: 1,
				category: new Category(),
				streams: [s1, s2],
				previewStream: s3,
				owner: Mock(SecUser)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 1
		p.errors.fieldErrors.get(0).field == "previewStream"
	}

	void "previewStream() validator passes if previewStream is included in streams"() {
		def s1 = new Stream(name: "stream-1")
		def s2 = new Stream(name: "stream-2")
		def s3 = new Stream(name: "stream-3")
		s1.id = "1"
		s2.id = "2"
		s3.id = "3"

		def p = new Product(
				name: "name",
				description: "description",
				ownerAddress: "0x0000000000000000000000000000000000000000",
				beneficiaryAddress: "0x0000000000000000000000000000000000000000",
				pricePerSecond: 1,
				category: new Category(),
				streams: [s1, s2, s3],
				previewStream: s3,
				owner: Mock(SecUser)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 0
	}

	void "Product passes validation if ownerAddress=null and beneficiaryAddress=null when pricePerSecond=0"() {
		def s1 = new Stream(name: "stream-1")
		def s2 = new Stream(name: "stream-2")
		def s3 = new Stream(name: "stream-3")
		s1.id = "1"
		s2.id = "2"
		s3.id = "3"

		def p = new Product(
			name: "name",
			description: "description",
			pricePerSecond: 0,
			category: new Category(),
			streams: [s1, s2, s3],
			previewStream: s3,
			owner: Mock(SecUser)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 0
	}

	void "Product does not pass validation if ownerAddress != null and pricePerSecond=0"() {
		def s1 = new Stream(name: "stream-1")
		def s2 = new Stream(name: "stream-2")
		def s3 = new Stream(name: "stream-3")
		s1.id = "1"
		s2.id = "2"
		s3.id = "3"

		def p = new Product(
			name: "name",
			description: "description",
			pricePerSecond: 0,
			ownerAddress: "0x0000000000000000000000000000000000000000",
			category: new Category(),
			streams: [s1, s2, s3],
			previewStream: s3,
			owner: Mock(SecUser)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 1
		p.errors.fieldErrors.get(0).field == "pricePerSecond"
	}

	void "Product does not pass validation if beneficiaryAddress != null and pricePerSecond=0"() {
		def s1 = new Stream(name: "stream-1")
		def s2 = new Stream(name: "stream-2")
		def s3 = new Stream(name: "stream-3")
		s1.id = "1"
		s2.id = "2"
		s3.id = "3"

		def p = new Product(
			name: "name",
			description: "description",
			pricePerSecond: 0,
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			category: new Category(),
			streams: [s1, s2, s3],
			previewStream: s3,
			owner: Mock(SecUser)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 1
		p.errors.fieldErrors.get(0).field == "pricePerSecond"
	}

	void "Product must have ownerAddress and beneficiaryAddress to pass validation if pricePerSecond != 0"() {
		def s1 = new Stream(name: "stream-1")
		def s2 = new Stream(name: "stream-2")
		def s3 = new Stream(name: "stream-3")
		s1.id = "1"
		s2.id = "2"
		s3.id = "3"

		def p = new Product(
			name: "name",
			description: "description",
			pricePerSecond: 5,
			category: new Category(),
			streams: [s1, s2, s3],
			previewStream: s3,
			owner: Mock(SecUser)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 1
		p.errors.fieldErrors.get(0).field == "pricePerSecond"
	}
}
