package com.unifina.service

import com.unifina.domain.*
import spock.lang.Specification

class ProductUpdateCommandSpec extends Specification {

	Product product
    ProductUpdateCommand command
	PermissionService permissionService

	void setup() {
		permissionService = Mock(PermissionService)
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
			owner: new User(name: "John Doe"),
			ownerAddress: "0x0",
			beneficiaryAddress: "0x0",
			pricePerSecond: 5,
			priceCurrency: Product.Currency.DATA,
			minimumSubscriptionInSeconds: 0
		)
		product.id = "product-id"


		// Set up ProductUpdateCommand
		Category newCategory = new Category(name: "new-category")
		newCategory.id = "new-category-id"

		Stream newStream = new Stream(name: "new-stream")
		newStream.id = "new-stream-id"

		command = new ProductUpdateCommand(
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
		command.updateProduct(product, new User(), permissionService)

		then:
		thrown(FieldCannotBeUpdatedException)
	}

	void "updateProduct() updates all off-chain fields on a deployed paid product"() {
		product.state = Product.State.DEPLOYED
		product.pricePerSecond = 5

		// Don't provide any of the on-chain fields
		ProductUpdateCommand.onChainFields.each {
			command[it] = null
		}

		when:
		command.updateProduct(product, new User(), permissionService)

		then:
		def map = product.toMap()
		map.id == "product-id"
		map.type == "NORMAL"
		map.state == "DEPLOYED"
		map.created == null
		map.updated == null
		map.owner == "John Doe"
		map.name == "new name"
		map.description == "new description"
		map.imageUrl == "image.jpg"
		map.thumbnailUrl == "thumb.jpg"
		map.category == "new-category-id"
		map.streams == ["new-stream-id"]
		map.previewStream == "new-stream-id"
		map.previewConfigJson == "{newConfig: true}"
		map.ownerAddress == "0x0"
		map.beneficiaryAddress == "0x0"
		map.pricePerSecond == "5"
		map.isFree == false
		map.priceCurrency == "DATA"
		map.minimumSubscriptionInSeconds == 0L
	}

	void "updateProduct() updates both on-chain and off-chain fields on non-deployed paid Products"() {
		product.state = Product.State.NOT_DEPLOYED
		product.pricePerSecond = 5

		when:
		command.updateProduct(product, new User(), permissionService)

		then:
		def map = product.toMap()
		map.id == "product-id"
		map.type == "NORMAL"
		map.state == "NOT_DEPLOYED"
		map.created == null
		map.updated == null
		map.owner == "John Doe"
		map.name == "new name"
		map.description == "new description"
		map.imageUrl == "image.jpg"
		map.thumbnailUrl == "thumb.jpg"
		map.category == "new-category-id"
		map.streams == ["new-stream-id"]
		map.previewStream == "new-stream-id"
		map.previewConfigJson == "{newConfig: true}"
		map.ownerAddress == "0xA"
		map.beneficiaryAddress == "0xF"
		map.pricePerSecond == "10"
		map.isFree == false
		map.priceCurrency == "USD"
		map.minimumSubscriptionInSeconds == 10L
	}

	void "updateProduct() updates both on-chain and off-chain fields on deployed free Products"() {
		product.state = Product.State.DEPLOYED
		product.pricePerSecond = 0
		command.pricePerSecond = 0

		when:
		command.updateProduct(product, new User(), permissionService)

		then:
		def map = product.toMap()
		map.id == "product-id"
		map.type == "NORMAL"
		map.state == "DEPLOYED"
		map.created == null
		map.updated == null
		map.owner == "John Doe"
		map.name == "new name"
		map.description == "new description"
		map.imageUrl == "image.jpg"
		map.thumbnailUrl == "thumb.jpg"
		map.category == "new-category-id"
		map.streams == ["new-stream-id"]
		map.previewStream == "new-stream-id"
		map.previewConfigJson == "{newConfig: true}"
		map.ownerAddress == "0xA"
		map.beneficiaryAddress == "0xF"
		map.pricePerSecond == "0"
		map.isFree == true
		map.priceCurrency == "USD"
		map.minimumSubscriptionInSeconds == 10L
	}

	void "updateProduct() throws when trying to change a free product to paid product when in deployed state"() {
		product.state = Product.State.DEPLOYED
		product.pricePerSecond = 0
		command.pricePerSecond = 5

		when:
		command.updateProduct(product, new User(), permissionService)

		then:
		thrown(FieldCannotBeUpdatedException)
	}

	void "updateProduct() throws when pendingChanges field is given and user doesn't have share permission"() {
		setup:
		command = new ProductUpdateCommand(
			name: "new name",
			description: "new description",
			pendingChanges: [name:"new name",description:"new description"]
		)
		product.pricePerSecond = 5

		when:
		command.updateProduct(product, new User(), permissionService)
		then:
		1 * permissionService.check(_, product, Permission.Operation.PRODUCT_SHARE) >> false
		thrown(FieldCannotBeUpdatedException)
	}

	void "updateProduct() updates pendingChanges if it is given and user has share permission"() {
		setup:
		command = new ProductUpdateCommand(
			name: "new name",
			description: "new description",
			pendingChanges: [name:"new name",description:"new description"]
		)
		product.pricePerSecond = 5

		when:
		command.updateProduct(product, new User(), permissionService)
		then:
		1 * permissionService.check(_, product, Permission.Operation.PRODUCT_SHARE) >> true
		product.pendingChanges == '''{"name":"new name","description":"new description"}'''
	}

	void "updateProduct() doesn't check sharing permission when pendingChanges is not given"() {
		setup:
		command = new ProductUpdateCommand(
			name: "new name",
			description: "new description",
		)
		product.pricePerSecond = 5

		when:
		command.updateProduct(product, new User(), permissionService)
		then:
		0 * permissionService.check(_, product, Permission.Operation.PRODUCT_SHARE)
	}
}
