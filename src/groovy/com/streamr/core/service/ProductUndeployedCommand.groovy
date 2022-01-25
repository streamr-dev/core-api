package com.streamr.core.service

import com.streamr.core.domain.Product
import grails.validation.Validateable

@Validateable
class ProductUndeployedCommand implements StalenessCheck {
	static constraints = {
		importFrom(Product)
	}
}
