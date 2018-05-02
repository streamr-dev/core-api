package com.unifina.api

import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.SecUser
import spock.lang.Specification

class UpdateProductCommandSpec extends Specification {

	Product product
	UpdateProductCommand command

	void setup() {
		// Set up Product
		Category category = new Category(name: "category")
		category.id = 'category-id'

		Stream stream = new Stream(name: "stream")
		stream.id = "stream-id"

		product = new Product(
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


		// Set up UpdateProductCommand
		Category newCategory = new Category(name: "new-category")
		newCategory.id = "new-category-id"

		Stream newStream = new Stream(name: "new-stream")
		newStream.id = "new-stream-id"

		command = new UpdateProductCommand(
			name: "new name",
			description: "new description",
			streams: [newStream],
			category: newCategory,
			previewStream: newStream,
			previewConfigJson: "{newConfig: true}",
			ownerAddress: "0xA",
			beneficiaryAddress: "0xF",
			pricePerSecond: 10,
			priceCurrency: Product.Currency.USD,
			minimumSubscriptionInSeconds: 10
		)
	}

	void "updateProduct() updates only non-blockchain fields of free Product"() {
		product.state = Product.State.NOT_DEPLOYED
		product.pricePerSecond = 0

		when:
		command.updateProduct(product)

		then:
		product.toMap() == [
			id: "product-id",
			state: "NOT_DEPLOYED",
			created: null,
			updated: null,
			owner: "John Doe",
			name: "new name",
			description: "new description",
			imageUrl: "image.jpg",
			thumbnailUrl: "thumb.jpg",
			category: "new-category-id",
			streams: ["new-stream-id"],
			previewStream: "new-stream-id",
			previewConfigJson: "{newConfig: true}",
			ownerAddress: "0x0",
			beneficiaryAddress: "0x0",
			pricePerSecond: "0",
			isFree: true,
			priceCurrency: "DATA",
			minimumSubscriptionInSeconds: 0L,
		]
	}

	void "updateProduct() updates only non-blockchain fields of deployed paid Product"() {
		product.state = Product.State.DEPLOYED

		when:
		command.updateProduct(product)

		then:
		product.toMap() == [
		    id: "product-id",
			state: "DEPLOYED",
			created: null,
			updated: null,
			owner: "John Doe",
			name: "new name",
			description: "new description",
			imageUrl: "image.jpg",
			thumbnailUrl: "thumb.jpg",
			category: "new-category-id",
			streams: ["new-stream-id"],
			previewStream: "new-stream-id",
			previewConfigJson: "{newConfig: true}",
			ownerAddress: "0x0",
			beneficiaryAddress: "0x0",
			pricePerSecond: "5",
			isFree: false,
			priceCurrency: "DATA",
			minimumSubscriptionInSeconds: 0L
		]
	}

	void "updateProduct() updates non-blockchain and blockchain fields of undeployed paid Product"() {
		product.state = Product.State.NOT_DEPLOYED

		when:
		command.updateProduct(product)

		then:
		product.toMap() == [
			id: "product-id",
			state: "NOT_DEPLOYED",
			created: null,
			updated: null,
			owner: "John Doe",
			name: "new name",
			description: "new description",
			imageUrl: "image.jpg",
			thumbnailUrl: "thumb.jpg",
			category: "new-category-id",
			streams: ["new-stream-id"],
			previewStream: "new-stream-id",
			previewConfigJson: "{newConfig: true}",
			ownerAddress: "0xA",
			beneficiaryAddress: "0xF",
			pricePerSecond: "10",
			isFree: false,
			priceCurrency: "USD",
			minimumSubscriptionInSeconds: 10L
		]
	}

	void "updateProduct() throws InvalidStateException if trying to set price = 0 of undeployed paid Product"() {
		product.state = Product.State.NOT_DEPLOYED

		when:
		command.pricePerSecond = 0
		command.updateProduct(product)

		then:
		thrown(InvalidStateException)
	}
}
