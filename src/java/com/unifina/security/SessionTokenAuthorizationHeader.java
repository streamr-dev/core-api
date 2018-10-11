package com.unifina.security;

import com.unifina.domain.security.SecUser;
import com.unifina.service.SessionService;
import grails.util.Holders;

public class SessionTokenAuthorizationHeader extends AuthorizationHeader {
	private String token;
	SessionService sessionService = Holders.getApplicationContext().getBean(SessionService.class);

	public SessionTokenAuthorizationHeader(String token) {
		this.token = token;
	}

	@Override
	public String getHeaderName() {
		return "Bearer";
	}

	@Override
	public AuthenticationResult computeAuthenticationResult() {
		if (token == null) {
			return new AuthenticationResult(true, false);
		}
		SecUser user = sessionService.getUserFromToken(token);
		if (user != null) {
			return new AuthenticationResult(user);
		}
		return new AuthenticationResult(false, false);
	}
}
