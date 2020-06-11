package com.unifina.api

import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
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
		address(blank: false)
	}
}
