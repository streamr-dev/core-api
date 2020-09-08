package com.unifina.service

import com.unifina.domain.Product
import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class DataUnionJoinRequestCommand {
	String memberAddress
	String secret
	Map<String, Object> metadata
	static constraints = {
		memberAddress(nullable: false, validator: Product.isEthereumAddress)
	}
}
