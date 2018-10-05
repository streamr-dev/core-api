package com.unifina.controller.api
import grails.validation.Validateable

@Validateable
class LoginCommand {
	enum Method {
		ETHEREUM,
		PASSWORD
	}

	Method method
	String username
	String password

	static constraints = {
		method(blank: false, nullable: false)
	}
}
