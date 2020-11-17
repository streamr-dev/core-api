package com.unifina.controller;

import com.unifina.domain.User;
import com.unifina.domain.Userish;
import com.unifina.service.InvalidSessionTokenException;
import com.unifina.service.SessionService;
import com.unifina.service.EthereumIntegrationKeyService;
import com.unifina.security.ApiKeyConverter;

import javax.servlet.http.HttpServletRequest;

public class TokenAuthenticator {
	private final SessionService sessionService;
	private final EthereumIntegrationKeyService ethereumIntegrationKeyService;

	private enum HeaderType {
		TOKEN,
		BEARER
	}

	public TokenAuthenticator(SessionService sessionService, EthereumIntegrationKeyService ethereumIntegrationKeyService) {
		this.sessionService = sessionService;
		this.ethereumIntegrationKeyService = ethereumIntegrationKeyService;
	}

	public static class AuthorizationHeader {
		private final HeaderType headerType;
		private final String headerValue;

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
			return new AuthenticationResult(true, true);
		}
		if (header == null) {
			return new AuthenticationResult(false, false);
		}
		if (header.getHeaderType().equals(HeaderType.TOKEN)) {
			AuthenticationResult res = getResultFromApiKey(header.getHeaderValue());
			return res;
		} else if (header.getHeaderType().equals(HeaderType.BEARER)) {
			AuthenticationResult res = getResultFromSessionToken(header.getHeaderValue());
			return res;
		}
		return new AuthenticationResult(false, true);
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
			return new AuthenticationResult(false, true);
		}
		try {
			Userish userish = sessionService.getUserishFromToken(token);
			if (userish instanceof User) {
				return new AuthenticationResult((User) userish);
			} else {
				throw new InvalidSessionTokenException("Invalid token: "+token);
			}
		} catch (InvalidSessionTokenException e) {
			return new AuthenticationResult(false, true);
		}
	}

	public AuthenticationResult getResultFromApiKey(String apiKey) {
		if (apiKey == null) {
			return new AuthenticationResult(false, true);
		}
		String privateKey = ApiKeyConverter.createEthereumPrivateKey(apiKey);
		String address = "0x" + EthereumIntegrationKeyService.getAddress(privateKey);
		User user = ethereumIntegrationKeyService.getEthereumUser(address);
		if (user != null) {
			return new AuthenticationResult(user);
		}
		return new AuthenticationResult(false, true);
	}

	private static class AuthenticationMalformedException extends RuntimeException {
	}

}

