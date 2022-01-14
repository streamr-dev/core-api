package com.unifina.domain

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Product)
@Mock([User, Product])
class ProductSpec extends Specification {
	void "previewStream() validator passes if previewStream = null and streams empty"() {
		def p = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 1,
			category: new Category(),
			owner: Mock(User)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 0
	}

	void "previewStream() validator does not pass if previewStream != null and streams empty"() {
		String s1 = "0x0000000000000000000000000000000000000001/abc"

		def p = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 1,
			category: new Category(),
			previewStreamId: s1,
			owner: Mock(User)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 1
		p.errors.fieldErrors.get(0).field == "previewStreamId"
	}

	void "previewStream() validator does not pass if previewStream not included in streams"() {
		def s1 = "0x0000000000000000000000000000000000000001/abc"
		def s2 = "0x0000000000000000000000000000000000000000/def"
		def s3 = "0x0000000000000000000000000000000000000000/ghj"

		def p = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 1,
			category: new Category(),
			streams: [s1, s2],
			previewStreamId: s3,
			owner: Mock(User)
		)
		when:
		p.validate()
		then:
		p.errors.errorCount == 1
		p.errors.fieldErrors.get(0).field == "previewStreamId"
	}

	void "previewStream() validator passes if previewStream is included in streams"() {
		def s1 = "0x0000000000000000000000000000000000000001/abc"
		def s2 = "0x0000000000000000000000000000000000000000/def"
		def s3 = "0x0000000000000000000000000000000000000000/ghj"

		def p = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0x0000000000000000000000000000000000000000",
			pricePerSecond: 1,
			category: new Category(),
			streams: [s1, s2, s3],
			previewStreamId: s3,
			owner: Mock(User)
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

		Product product = new Product(
			name: "name",
			description: "description",
			imageUrl: "image.jpg",
			thumbnailUrl: "thumb.jpg",
			category: category,
			state: Product.State.DEPLOYED,
			previewStreamId: "0x0000000000000000000000000000000000000001/abc",
			streams: ["0x0000000000000000000000000000000000000001/abc"],
			previewConfigJson: "{}",
			score: 5,
			owner: new User(name: "John Doe"),
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
		map.streams == ["0x0000000000000000000000000000000000000001/abc"]
		map.previewStreamId == "0x0000000000000000000000000000000000000001/abc"
		map.previewConfigJson == "{}"
		map.ownerAddress == "0x0"
		map.beneficiaryAddress == "0x0"
		map.pricePerSecond == "5"
		map.isFree == false
		map.priceCurrency == "DATA"
		map.minimumSubscriptionInSeconds == 0L
		map.pendingChanges == [name: "new name"]
	}

	@Unroll
	void "isFree(#price) == #expected"(Long price, Object expected) {
		expect:
		Product p = new Product(pricePerSecond: price)
		p.isFree() == expected

		where:
		price | expected
		-1L   | false
		0L    | true
		1L    | false
	}
}
