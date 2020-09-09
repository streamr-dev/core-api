package com.unifina.controller

import grails.validation.Validateable

@Validateable
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
