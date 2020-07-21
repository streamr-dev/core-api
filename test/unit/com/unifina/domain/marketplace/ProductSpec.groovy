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
	void "pendingChanges field is shown to the owner"() {
		setup:
		Category category = new Category(name: "category")
		category.id = 'category-id'

		Stream stream = new Stream(name: "stream")
		stream.id = "stream-id"

		Product product = new Product(
			name: "name",
			description: "description",
			imageUrl: "image.jpg",
			thumbnailUrl: "thumb.jpg",
			category: category,
			state: Product.State.DEPLOYED,
			previewStream: stream,
			streams: [stream],
			previewConfigJson: "{}",
			score: 5,
			owner: new SecUser(name: "John Doe"),
			ownerAddress: "0x0",
			beneficiaryAddress: "0x0",
			pricePerSecond: 5,
			priceCurrency: Product.Currency.DATA,
			minimumSubscriptionInSeconds: 0
		)
		product.id = "product-id"

		when:
		product.pendingChanges = '{"name":"new name"}'

		then:
		def map = product.toMap(true)
		map.id == "product-id"
		map.type == "NORMAL"
		map.state == "DEPLOYED"
		map.created == null
		map.updated == null
		map.owner == "John Doe"
		map.name == "name"
		map.description == "description"
		map.imageUrl == "image.jpg"
		map.thumbnailUrl == "thumb.jpg"
		map.category == "category-id"
		map.streams == ["stream-id"]
		map.previewStream == "stream-id"
		map.previewConfigJson == "{}"
		map.ownerAddress == "0x0"
		map.beneficiaryAddress == "0x0"
		map.pricePerSecond == "5"
		map.isFree == false
		map.priceCurrency == "DATA"
		map.minimumSubscriptionInSeconds == 0L
		map.pendingChanges == [name: "new name"]
	}
}
