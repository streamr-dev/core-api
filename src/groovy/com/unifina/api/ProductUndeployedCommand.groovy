package com.unifina.api


import com.unifina.domain.marketplace.Product
import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class ProductUndeployedCommand implements StalenessCheck {
	static constraints = {
		importFrom(Product)
	}
}
