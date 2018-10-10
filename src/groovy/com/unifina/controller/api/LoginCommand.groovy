package com.unifina.controller.api
import grails.validation.Validateable

@Validateable
class LoginCommand {
	enum Method {
		ETHEREUM,
		PASSWORD,
		APIKEY
	}

	Method method
	String username
	String password
	String apiKey

	static constraints = {
		method(blank: false, nullable: false)
	}
}
