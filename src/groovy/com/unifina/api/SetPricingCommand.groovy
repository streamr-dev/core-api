package com.unifina.api

import com.unifina.domain.marketplace.Product
import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class SetPricingCommand implements StalenessCheck {
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Product.Currency priceCurrency
	Long minimumSubscriptionInSeconds
}
