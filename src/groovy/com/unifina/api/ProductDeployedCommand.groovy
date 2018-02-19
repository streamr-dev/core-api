package com.unifina.api

import com.unifina.domain.marketplace.Product
import grails.validation.Validateable

@Validateable
class ProductDeployedCommand {
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Product.Currency priceCurrency
	Long minimumSubscriptionInSeconds

	static constraints = {
		importFrom(Product)
	}
}
