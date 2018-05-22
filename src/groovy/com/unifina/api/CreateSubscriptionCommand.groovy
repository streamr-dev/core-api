package com.unifina.api

import com.unifina.domain.marketplace.Product
import grails.validation.Validateable

@Validateable
class CreateSubscriptionCommand {
	Product product
	String address
	Long endsAt

	static constraints = {
		address(nullable: true, validator: Product.isEthereumAddressOrIsNull)
		endsAt(min: 0L)
	}

	Date getEndsAtAsDate() {
		return endsAt ? new Date(endsAt * 1000) : null
	}
}
