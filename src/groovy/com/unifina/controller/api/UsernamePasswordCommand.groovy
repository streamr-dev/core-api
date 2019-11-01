package com.unifina.controller.api

import grails.validation.Validateable

@Validateable
class UsernamePasswordCommand {
	String username
	String password
	static constraints = {
		username(blank: false, nullable: false)
		password(blank: false, nullable: false)
	}
}
