package com.unifina.controller;

import com.unifina.domain.Key;
import com.unifina.domain.User;
import com.unifina.domain.Userish;
import com.unifina.service.InvalidSessionTokenException;
import com.unifina.service.SessionService;
import grails.util.Holders;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.servlet.http.HttpServletRequest;

public class TokenAuthenticator {
	SessionService sessionService = Holders.getApplicationContext().getBean(SessionService.class);
	private enum HeaderType {
		TOKEN,
		BEARER
	}
	public static class AuthorizationHeader {

		private HeaderType headerType;
		private String headerValue;

		public AuthorizationHeader(HeaderType headerType, String headerValue) {
			this.headerType = headerType;
			this.headerValue = headerValue;
		}

		public HeaderType getHeaderType() {
			return headerType;
		}

		public String getHeaderValue() {
			return headerValue;
		}

		public String toString() {
			return headerType + " " + headerValue;
		}
	}
	public AuthenticationResult authenticate(HttpServletRequest request) {
		AuthorizationHeader header;
		try {
			header = getAuthorizationHeader(request);
		} catch (AuthenticationMalformedException e) {
			return new AuthenticationResult(false, true, true);
		}
		if (header == null) {
			return new AuthenticationResult(true, false, false);
		}
		if (header.getHeaderType().equals(HeaderType.TOKEN)) {
			AuthenticationResult res = getResultFromApiKey(header.getHeaderValue());
			return res;
		} else if (header.getHeaderType().equals(HeaderType.BEARER)) {
			AuthenticationResult res = getResultFromSessionToken(header.getHeaderValue());
			return res;
		}
		return new AuthenticationResult(true, false, true);
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
			if (parts[0].toLowerCase().equals("token")) {
				return new AuthorizationHeader(HeaderType.TOKEN, parts[1]);
			} else if (parts[0].toLowerCase().equals("bearer")) {
				return new AuthorizationHeader(HeaderType.BEARER, parts[1]);
			} else {
				throw new AuthenticationMalformedException();
			}
		}
		return null;
	}

	public String getSessionToken(HttpServletRequest request) {
		try {
			AuthorizationHeader header = parseAuthorizationHeader(request.getHeader("Authorization"));
			if (header == null || !header.getHeaderType().equals(HeaderType.BEARER)) {
				return null;
			}
			return header.getHeaderValue();
		} catch (AuthenticationMalformedException e) {
			return null;
		}
	}

	private AuthenticationResult getResultFromSessionToken(String token) {
		if (token == null) {
			return new AuthenticationResult(true, false, true);
		}
		try {
			Userish userish = sessionService.getUserishFromToken(token);
			if (userish instanceof User) {
				return new AuthenticationResult((User) userish);
			} else if (userish instanceof Key) {
				return new AuthenticationResult((Key) userish);
			} else {
				throw new InvalidSessionTokenException("Invalid token: "+token);
			}
		} catch (InvalidSessionTokenException e) {
			return new AuthenticationResult(false, false, true);
		}
	}

	public AuthenticationResult getResultFromApiKey(String apiKey) {
		if (apiKey == null) {
			return new AuthenticationResult(true, false, true);
		}
		Key keyObject = (Key) InvokerHelper.invokeMethod(Key.class, "get", apiKey);
		if (keyObject != null) {
			return new AuthenticationResult(keyObject);
		}
		return new AuthenticationResult(false, false, true);
	}

	private static class AuthenticationMalformedException extends RuntimeException {
	}

}

