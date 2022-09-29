package com.streamr.core.service

import com.streamr.core.domain.Product
import grails.validation.Validateable

@Validateable
class SetPricingCommand implements StalenessCheck {
	String ownerAddress
	String beneficiaryAddress
	String pricePerSecond
	Product.Currency priceCurrency
	Long minimumSubscriptionInSeconds

	static constraints = {
	}
}
