package com.unifina.security;

import com.unifina.domain.security.Key;
import com.unifina.domain.security.SecUser;
import com.unifina.service.SessionService;
import grails.util.Holders;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.servlet.http.HttpServletRequest;

public class TokenAuthenticator {
	SessionService sessionService = Holders.getApplicationContext().getBean(SessionService.class);

	public AuthenticationResult authenticate(HttpServletRequest request) {
		String[] header;
		try {
			header = parseAuthorizationHeader(request.getHeader("Authorization"));
		} catch (AuthenticationMalformedException e) {
			return new AuthenticationResult(false, true);
		}
		if (header == null) {
			return new AuthenticationResult(true, false);
		}
		if (header[0].equals("Token")) {
			AuthenticationResult res = getResultFromApiKey(header[1]);
			res.setAuthorizationHeaderName("Token");
			return res;
		} else if (header[0].equals("Bearer")) {
			AuthenticationResult res = getResultFromSessionToken(header[1]);
			res.setAuthorizationHeaderName("Bearer");
			return res;
		}
		return new AuthenticationResult(true, false);
	}

	public String[] parseAuthorizationHeader(String s) {
		s = s == null ? null : s.trim();
		if (s != null && !s.isEmpty()) {
			String[] parts = s.split("\\s+");
			if (parts.length != 2) {
				throw new AuthenticationMalformedException();
			}
			if (parts[0].toLowerCase().equals("Token".toLowerCase())) {
				String[] res = {"Token", parts[1]};
				return res;
			} else if (parts[0].toLowerCase().equals("Bearer".toLowerCase())) {
				String[] res = {"Bearer", parts[1]};
				return res;
			} else {
				throw new AuthenticationMalformedException();
			}
		}
		return null;
	}

	private AuthenticationResult getResultFromSessionToken(String token) {
		if (token == null) {
			return new AuthenticationResult(true, false);
		}
		SecUser user = sessionService.getUserFromToken(token);
		if (user != null) {
			return new AuthenticationResult(user);
		}
		return new AuthenticationResult(false, false);
	}

	public AuthenticationResult getResultFromApiKey(String apiKey) {
		if (apiKey == null) {
			return new AuthenticationResult(true, false);
		}
		Key keyObject = (Key) InvokerHelper.invokeMethod(Key.class, "get", apiKey);
		if (keyObject != null) {
			return new AuthenticationResult(keyObject);
		}
		return new AuthenticationResult(false, false);
	}

	private static class AuthenticationMalformedException extends RuntimeException {
	}

}
