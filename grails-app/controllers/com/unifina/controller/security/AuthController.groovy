package com.unifina.controller.security

import grails.plugin.springsecurity.annotation.Secured
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.security.web.savedrequest.SavedRequest

@Secured(["permitAll"])
class AuthController {

	RequestCache requestCache

	static allowedMethods = [
		register      : "POST",
		signup        : "POST",
		forgotPassword: "POST",
		resetPassword : "POST",
	]

	static layout = 'app'

	def index = {
		return
	}

	def fullAuth = {
		SavedRequest savedRequest = requestCache.getRequest(request, response)
		redirect action: "index", params: [
			redirect     : savedRequest.getRedirectUrl(),
			ignoreSession: true
		]
	}

}
