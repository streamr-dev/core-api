package com.unifina.api

import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class ChallengeResponseCommand {
	ChallengeCommand challenge
	String signature
	String address

	static constraints = {
		challenge(blank: false, nullable: false)
		signature(blank: false, nullable: false)
		address(blank: false, nullable: false)
	}
}
