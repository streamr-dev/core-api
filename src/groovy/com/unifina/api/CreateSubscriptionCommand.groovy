package com.unifina.api

import com.unifina.domain.marketplace.Product
import grails.validation.Validateable
import org.grails.databinding.BindingFormat

@Validateable
class CreateSubscriptionCommand {
	Product product
	String address
	@BindingFormat("yyyy-MM-dd'T'hh:mm:ss'Z'")
	Date endsAt
}
