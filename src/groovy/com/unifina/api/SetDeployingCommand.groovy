package com.unifina.api

import com.unifina.domain.marketplace.Product
import grails.validation.Validateable

@Validateable
class SetDeployingCommand {
	String tx

	static constraints = {
		tx(validator: Product.isEthereumTransaction)
	}
}
