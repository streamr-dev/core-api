package com.unifina.security;

import com.unifina.domain.security.Key;
import com.unifina.domain.security.SecUser;
import com.unifina.service.UserService;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.servlet.http.HttpServletRequest;

public class TokenAuthenticator {
	private boolean lastAuthenticationMalformed = false;
	private boolean keyPresent = false;

	public AuthenticationResult authenticate(HttpServletRequest request) {
		String key = parseAuthorizationHeader(request.getHeader("Authorization"));
		keyPresent = key != null;

		if (!keyPresent) {
			return null;
		}

		Key keyObject = (Key) InvokerHelper.invokeMethod(Key.class, "get", key);
		if (keyObject != null) {
			return new AuthenticationResult(keyObject);
		}

		return new AuthenticationResult();
	}

	public boolean lastAuthenticationMalformed() {
		return lastAuthenticationMalformed;
	}

	public boolean isKeyPresent() {
		return keyPresent;
	}

	/**
	 * "Authorization: Token apiKey" => "apiKey"
	 */
	private String parseAuthorizationHeader(String s) {
		s = s == null ? null : s.trim();
		if (s != null && !s.isEmpty()) {
			String[] parts = s.split("\\s+");
			if (parts.length == 2 && parts[0].toLowerCase().equals("token")) {
				lastAuthenticationMalformed = false;
				return parts[1];
			} else {
				lastAuthenticationMalformed = true;
			}
 		}
		return null;
	}
}
