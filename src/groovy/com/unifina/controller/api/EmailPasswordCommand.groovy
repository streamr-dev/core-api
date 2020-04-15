package com.unifina.controller.api

import grails.validation.Validateable

@Validateable
class EmailPasswordCommand {
	String email
	String password
	static constraints = {
		email(blank: false, nullable: false)
		password(blank: false, nullable: false)
	}
}
