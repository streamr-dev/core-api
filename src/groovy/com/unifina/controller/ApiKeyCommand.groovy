package com.unifina.controller

import grails.validation.Validateable

@Validateable
class ApiKeyCommand{
	String apiKey
	static constraints = {
		apiKey(blank: false, nullable: false)
	}
}
