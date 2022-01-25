package com.streamr.core.controller

import com.streamr.core.service.SessionService
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
