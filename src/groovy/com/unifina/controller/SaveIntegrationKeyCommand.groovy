package com.unifina.controller

import com.unifina.domain.EthereumAddressValidator
import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable

@Validateable
@GrailsCompileStatic
class SaveIntegrationKeyCommand {
	String name
	String service
	ChallengeCommand challenge
	String signature
	String address
	Map json
	static constraints = {
		name(blank: false)
		service(blank: false)
		signature(blank: false)
		address(blank: false, validator: EthereumAddressValidator.validate)
	}
}
