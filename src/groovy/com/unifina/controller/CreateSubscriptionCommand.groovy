package com.unifina.controller

import com.unifina.domain.EthereumAddressValidator
import com.unifina.domain.Product
import grails.validation.Validateable

@Validateable
class CreateSubscriptionCommand {
	Product product
	String address
	Long endsAt

	static constraints = {
		address(nullable: true, validator: EthereumAddressValidator.isNullOrValid)
		endsAt(min: 0L)
	}

	Date getEndsAtAsDate() {
		return endsAt ? new Date(endsAt * 1000) : null
	}
}
