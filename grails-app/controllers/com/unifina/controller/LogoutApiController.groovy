package com.unifina.controller

import com.unifina.security.StreamrApi
import com.unifina.security.TokenAuthenticator
import com.unifina.service.SessionService
import grails.converters.JSON

class LogoutApiController {
	SessionService sessionService

	@StreamrApi
    def logout() {
		TokenAuthenticator authenticator = new TokenAuthenticator()
		String sessionToken = authenticator.getSessionToken(request)
		if (sessionToken != null) {
			sessionService.invalidateSession(sessionToken)
		}
		render([:] as JSON)
	}
}
