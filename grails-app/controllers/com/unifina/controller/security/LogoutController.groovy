package com.unifina.controller.security

import com.unifina.security.TokenAuthenticator
import com.unifina.service.SessionService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

@Secured(["permitAll"])
class LogoutController {

	SessionService sessionService

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index = {
		// TODO put any pre-logout code here
		TokenAuthenticator authenticator = new TokenAuthenticator()
		String sessionToken = authenticator.getSessionToken(request)
		if (sessionToken != null) {
			sessionService.invalidateSession(sessionToken)
		}
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}
}
