package com.unifina.controller.api

import grails.validation.Validateable

@Validateable
class ApiKeyCommand {
	String apikey
	static constraints = {
		apikey(blank: false, nullable: false)
	}
}
