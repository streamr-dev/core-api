package com.unifina.api

import com.unifina.domain.marketplace.Product
import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class ProductDeployedCommand implements StalenessCheck {
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Product.Currency priceCurrency
	Long minimumSubscriptionInSeconds

	static constraints = {
		importFrom(Product)
	}
}
