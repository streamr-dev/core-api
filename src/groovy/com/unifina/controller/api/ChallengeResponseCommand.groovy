package com.unifina.controller.api

import grails.validation.Validateable

@Validateable
class ChallengeResponseCommand {
	ChallengeCommand challenge
	String signature
	String address
	static constraints = {
		signature(blank: false)
		address(blank: false)
	}
}
