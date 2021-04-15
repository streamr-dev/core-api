package com.unifina.controller;

import com.unifina.domain.User;
import com.unifina.domain.Userish;
import com.unifina.service.InvalidSessionTokenException;
import com.unifina.service.SessionService;
import grails.util.Holders;

import javax.servlet.http.HttpServletRequest;

public class TokenAuthenticator {
	private static final String BEARER = "Bearer";
	private final SessionService sessionService = Holders.getApplicationContext().getBean(SessionService.class);

	public static class AuthorizationHeader {
		private final String headerValue;

		public AuthorizationHeader(String headerValue) {
			this.headerValue = headerValue;
		}

		public String getHeaderValue() {
			return headerValue;
		}

		public String toString() {
			return BEARER + " " + headerValue;
		}
	}

	public AuthenticationResult authenticate(HttpServletRequest request) {
		AuthorizationHeader header;
		try {
			header = getAuthorizationHeader(request);
		} catch (AuthenticationMalformedException e) {
			return new AuthenticationResult(true, true);
		}
		if (header == null) {
			return new AuthenticationResult(false, false);
		}
		return getResultFromSessionToken(header.getHeaderValue());
	}

	public static AuthorizationHeader getAuthorizationHeader(HttpServletRequest request) {
		return parseAuthorizationHeader(request.getHeader("Authorization"));
	}

	public static AuthorizationHeader parseAuthorizationHeader(String s) {
		s = s == null ? null : s.trim();
		if (s != null && !s.isEmpty()) {
			String[] parts = s.split("\\s+");
			if (parts.length != 2) {
				throw new AuthenticationMalformedException();
			}
			if (parts[0].equalsIgnoreCase(BEARER)) {
				return new AuthorizationHeader(parts[1]);
			} else {
				throw new AuthenticationMalformedException();
			}
		}
		return null;
	}

	public String getSessionToken(HttpServletRequest request) {
		try {
			AuthorizationHeader header = parseAuthorizationHeader(request.getHeader("Authorization"));
			if (header == null) {
				return null;
			}
			return header.getHeaderValue();
		} catch (AuthenticationMalformedException e) {
			return null;
		}
	}

	private AuthenticationResult getResultFromSessionToken(String token) {
		if (token == null) {
			return new AuthenticationResult(false, true);
		}
		try {
			Userish userish = sessionService.getUserishFromToken(token);
			if (userish instanceof User) {
				return new AuthenticationResult((User) userish);
			} else {
				throw new InvalidSessionTokenException("Invalid token: " + token);
			}
		} catch (InvalidSessionTokenException e) {
			return new AuthenticationResult(false, true);
		}
	}

	private static class AuthenticationMalformedException extends RuntimeException {
	}
}

