package com.unifina.controller.help

import com.unifina.domain.security.SecUser
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class HelpController {

	def springSecurityService

	static defaultAction = "userGuide"

	def userGuide() {

	}

	def api() {
		[user: (SecUser) springSecurityService.currentUser]
	}
}