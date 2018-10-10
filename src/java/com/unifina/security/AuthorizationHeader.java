package com.unifina.security;

public abstract class AuthorizationHeader {
	public abstract String getHeaderName();
	protected AuthenticationResult authRes;

	public AuthenticationResult getAuthenticationResult(){
		if(authRes!=null){
			return authRes;
		}else{
			authRes = computeAuthenticationResult();
			return authRes;
		}
	}

	public abstract AuthenticationResult computeAuthenticationResult();
}
