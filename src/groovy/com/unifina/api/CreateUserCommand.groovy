package com.unifina.api

import com.unifina.utils.UsernameValidator
import grails.validation.Validateable

@Validateable
class CreateUserCommand {
	String username
	String password
	String name
	String timezone = "UTC"

	static constraints = {
		username(blank: false, validator: UsernameValidator.validate)
		password(blank: false)
		name(blank: false)
		timezone(nullable: true)
	}
}
