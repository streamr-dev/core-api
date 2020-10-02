package com.unifina.service


import com.unifina.domain.Product
import grails.validation.Validateable

@Validateable
class SetPricingCommand implements StalenessCheck {
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Product.Currency priceCurrency
	Long minimumSubscriptionInSeconds

	static constraints = {
	}
}
