package com.unifina.api

import com.unifina.domain.marketplace.Product
import grails.validation.Validateable

@Validateable
class ProductUndeployedCommand implements StalenessCheck {
	static constraints = {
		importFrom(Product)
	}
}
