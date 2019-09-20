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

	void "updateProduct() throws when trying to update on-chain fields on a deployed paid product"() {
		product.state = Product.State.DEPLOYED
		product.pricePerSecond = 5

		when:
		command.updateProduct(product)

		then:
		thrown(FieldCannotBeUpdatedException)
	}

	void "updateProduct() updates all off-chain fields on a deployed paid product"() {
		product.state = Product.State.DEPLOYED
		product.pricePerSecond = 5

		// Don't provide any of the on-chain fields
		UpdateProductCommand.onChainFields.each {
			command[it] = null
		}

		when:
		command.updateProduct(product)

		then:
		product.toMap() == [
			id: "product-id",
			type: "normal",
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

	void "updateProduct() updates both on-chain and off-chain fields on non-deployed paid Products"() {
		product.state = Product.State.NOT_DEPLOYED
		product.pricePerSecond = 5

		when:
		command.updateProduct(product)

		then:
		product.toMap() == [
			id: "product-id",
			type: "normal",
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

	void "updateProduct() updates both on-chain and off-chain fields on deployed free Products"() {
		product.state = Product.State.DEPLOYED
		product.pricePerSecond = 0
		command.pricePerSecond = 0

		when:
		command.updateProduct(product)

		then:
		product.toMap() == [
			id: "product-id",
			type: "normal",
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
			ownerAddress: "0xA",
			beneficiaryAddress: "0xF",
			pricePerSecond: "0",
			isFree: true,
			priceCurrency: "USD",
			minimumSubscriptionInSeconds: 10L
		]
	}

	void "updateProduct() throws when trying to change a free product to paid product when in deployed state"() {
		product.state = Product.State.DEPLOYED
		product.pricePerSecond = 0
		command.pricePerSecond = 5

		when:
		command.updateProduct(product)

		then:
		thrown(FieldCannotBeUpdatedException)
	}

}
