package com.unifina.security;

import com.unifina.domain.security.Key;
import org.codehaus.groovy.runtime.InvokerHelper;

public class ApiKeyAuthorizationHeader extends AuthorizationHeader{
	private String key;

	public ApiKeyAuthorizationHeader(String key){
		this.key = key;
	}

	@Override
	public String getHeaderName(){
		return "Token";
	}

	@Override
	public AuthenticationResult computeAuthenticationResult(){
		if (key == null) {
			return new AuthenticationResult(true, false);
		}
		Key keyObject = (Key) InvokerHelper.invokeMethod(Key.class, "get", key);
		if (keyObject != null) {
			return new AuthenticationResult(keyObject);
		}
		return new AuthenticationResult(false, false);
	}
}
