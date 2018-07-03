package com.unifina.controller.security

import grails.plugin.springsecurity.annotation.Secured

@Secured(["permitAll"])
class AuthController {

	def index = {
		return
	}
}
