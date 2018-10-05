package com.unifina.security;

import com.unifina.domain.security.Key;
import com.unifina.domain.security.SecUser;
import com.unifina.service.UserService;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.servlet.http.HttpServletRequest;

public class TokenAuthenticator {
	public AuthenticationResult authenticate(HttpServletRequest request) {
		String key;

		try {
			key = parseAuthorizationHeader(request.getHeader("Authorization"), "token");
		} catch (AuthenticationMalformedException e) {
			return new AuthenticationResult(false, true);
		}

		if (key == null) {
			return new AuthenticationResult(true, false);
		}

		Key keyObject = (Key) InvokerHelper.invokeMethod(Key.class, "get", key);
		if (keyObject != null) {
			return new AuthenticationResult(keyObject);
		}

		return new AuthenticationResult(false, false);
	}

	/**
	 * "Authorization: Token apiKey" => "apiKey"
	 */
	public String parseAuthorizationHeader(String s, String headerName) {
		s = s == null ? null : s.trim();
		if (s != null && !s.isEmpty()) {
			String[] parts = s.split("\\s+");
			if (parts.length == 2 && parts[0].toLowerCase().equals(headerName)) {
				return parts[1];
			} else {
				throw new AuthenticationMalformedException();
			}
 		}
		return null;
	}

	private static class AuthenticationMalformedException extends RuntimeException {
	}

}
