package com.streamr.core.controller

import grails.validation.Validateable

@Validateable
class ChallengeCommand {
	String id
	String challenge
	static constraints = {
		id(blank: false)
		challenge(blank: false)
	}
}
