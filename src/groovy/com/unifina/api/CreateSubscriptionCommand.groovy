package com.unifina.api

import com.unifina.domain.marketplace.Product
import grails.validation.Validateable
import org.grails.databinding.BindingFormat

@Validateable
class CreateSubscriptionCommand {
	Product product
	String address
	Long endsAt

	static constraints = {
		address(validator: Product.isEthereumAddress)
		endsAt(min: 0L)
	}
}
