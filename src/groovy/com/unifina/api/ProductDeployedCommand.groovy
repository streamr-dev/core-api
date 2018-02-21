package com.unifina.api

import com.unifina.domain.marketplace.Product
import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable

@Validateable
class ProductDeployedCommand {
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Product.Currency priceCurrency
	Long minimumSubscriptionInSeconds
	Long blockNumber
	Long blockIndex

	static constraints = {
		importFrom(Product)
	}

	/**
	 * Determines whether the data contained in this command is stale (older) compared to what is known in Product.
	 */
	boolean isStale(Product product) {
		blockNumber == product.blockNumber ? blockIndex <= product.blockIndex : blockNumber < product.blockNumber
	}
}
