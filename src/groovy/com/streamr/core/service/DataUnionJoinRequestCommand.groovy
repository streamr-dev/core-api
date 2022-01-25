package com.streamr.core.service

import com.streamr.core.domain.EthereumAddressValidator
import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class DataUnionJoinRequestCommand {
	String memberAddress
	String secret
	Map<String, Object> metadata
	static constraints = {
		memberAddress(nullable: false, validator: EthereumAddressValidator.validate)
	}
}
