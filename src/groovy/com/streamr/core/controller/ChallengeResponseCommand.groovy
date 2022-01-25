package com.streamr.core.controller

import com.streamr.core.domain.EthereumAddressValidator
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
