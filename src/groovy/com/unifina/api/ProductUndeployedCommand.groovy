package com.unifina.api

import com.unifina.domain.Product
import grails.validation.Validateable

@Validateable
class ProductUndeployedCommand implements StalenessCheck {
	static constraints = {
		importFrom(Product)
	}
}
