package com.unifina.security;

public class SessionTokenAuthorizationHeader extends AuthorizationHeader {
	private String token;

	public SessionTokenAuthorizationHeader(String token){
		this.token = token;
	}

	@Override
	public String getHeaderName() {
		return "Bearer";
	}

	@Override
	public AuthenticationResult computeAuthenticationResult() {
		return null;
	}
}
