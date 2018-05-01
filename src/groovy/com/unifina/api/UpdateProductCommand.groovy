package com.unifina.api

import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable

@Validateable
class UpdateProductCommand {
	String name
	String description

	Set<Stream> streams = []

	Category category
	Stream previewStream
	String previewConfigJson

	// Below are used only when updating NOT_DEPLOYED product
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Product.Currency priceCurrency
	Long minimumSubscriptionInSeconds

	static constraints = {
		name(blank: false)
		description(blank: false)
		streams(maxSize: 1000)
		previewStream(nullable: true)
		previewConfigJson(nullable: true)

		ownerAddress(nullable: true, validator: Product.isEthereumAddressOrIsNull)
		beneficiaryAddress(nullable: true, validator: Product.isEthereumAddressOrIsNull)
		pricePerSecond(nullable: true)
		priceCurrency(nullable: true)
		minimumSubscriptionInSeconds(nullable: true)
	}

	@GrailsCompileStatic
	void updateProduct(Product product) {
		product.name = name
		product.description = description
		product.streams = streams
		product.category = category
		product.previewStream = previewStream
		product.previewConfigJson = previewConfigJson

		if (product.pricePerSecond > 0 && product.state == Product.State.NOT_DEPLOYED) {
			if (pricePerSecond == 0) {
				throw new InvalidStateException("Cannot update paid Product (price > 0) to be free (price = 0)")
			}
			product.ownerAddress = ownerAddress
			product.beneficiaryAddress = beneficiaryAddress
			product.pricePerSecond = pricePerSecond
			product.priceCurrency = priceCurrency
			product.minimumSubscriptionInSeconds = minimumSubscriptionInSeconds
		}
	}
}
