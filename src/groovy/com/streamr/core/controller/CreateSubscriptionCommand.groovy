package com.streamr.core.controller

import com.streamr.core.domain.EthereumAddressValidator
import com.streamr.core.domain.Product
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
