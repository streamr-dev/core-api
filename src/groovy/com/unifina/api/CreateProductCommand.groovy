package com.unifina.api

import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import grails.validation.Validateable

@Validateable
class CreateProductCommand {
	String name
	String description

	Category category
	Stream previewStream
	String previewConfigJson

	Set<Stream> streams = []

	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Product.Currency priceCurrency = Product.Currency.DATA
	Long minimumSubscriptionInSeconds = 0

	static constraints = {
		importFrom(Product)
	}
}
