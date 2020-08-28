package com.streamr.client.authentication;

import com.unifina.domain.User;
import com.unifina.domain.SignupMethod;
import com.unifina.security.SessionToken;
import com.unifina.service.EthereumIntegrationKeyService;
import com.unifina.service.SessionService;

import java.io.IOException;

public class InternalAuthenticationMethod extends EthereumAuthenticationMethod {
	private final SessionService sessionService;
	private final User user;

	public InternalAuthenticationMethod(String ethereumPrivateKey, EthereumIntegrationKeyService ethereumIntegrationKeyService, SessionService sessionService, SignupMethod signupMethod) {
		super(ethereumPrivateKey);
		this.user = ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(this.getAddress(), signupMethod);
		this.sessionService = sessionService;
	}

	@Override
	protected AuthenticationMethod.LoginResponse login(String restApiUrl) throws IOException {
		final SessionToken sessionToken = sessionService.generateToken(user);
		return new AuthenticationMethod.LoginResponse(sessionToken.getToken(), sessionToken.getExpiration());
	}
}
