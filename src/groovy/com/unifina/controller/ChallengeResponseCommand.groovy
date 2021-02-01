package com.unifina.controller

import com.unifina.domain.EthereumAddressValidator
import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable

@Validateable
@GrailsCompileStatic
class ChallengeResponseCommand {
	ChallengeCommand challenge
	String signature
	String address
	static constraints = {
		challenge(nullable: false)
		signature(nullable: false)
		address(nullable: false, validator: EthereumAddressValidator.validate)
	}
}
