package com.unifina.controller.security

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

@Secured(["permitAll"])
class AuthController {

	/**
	 * Dependency injection for the springSecurityService.
	 */
	def springSecurityService

	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
		}
	}
}
