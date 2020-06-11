package com.unifina.api

import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString(excludes = ["password"])
class UsernamePasswordCommand {
	String username
	String password

	static constraints = {
		username(blank: false, nullable: false)
		password(blank: false, nullable: false)
	}
}
