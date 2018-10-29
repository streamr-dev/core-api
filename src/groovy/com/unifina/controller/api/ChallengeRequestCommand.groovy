package com.unifina.controller.api

import grails.validation.Validateable

@Validateable
class ChallengeRequestCommand {
	String address
	static constraints = {
		address(blank: false)
	}
}
