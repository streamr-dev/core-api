package com.streamr.client.authentication;

import com.google.gson.Gson;
import com.unifina.domain.security.SecUser;
import com.unifina.security.SessionToken;
import com.unifina.service.EthereumIntegrationKeyService;
import com.unifina.service.SessionService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

public class InternalAuthenticationMethod extends EthereumAuthenticationMethod {
	private final SessionService sessionService;
	private final SecUser user;

	public InternalAuthenticationMethod(String ethereumPrivateKey, EthereumIntegrationKeyService ethereumIntegrationKeyService, SessionService sessionService) {
		super(ethereumPrivateKey);
		this.user = ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(this.getAddress());
		this.sessionService = sessionService;
	}

	@Override
	protected AuthenticationMethod.LoginResponse login(String restApiUrl) throws IOException {
		final SessionToken sessionToken = sessionService.generateToken(user);
		return new AuthenticationMethod.LoginResponse(sessionToken.getToken(), sessionToken.getExpiration());
	}
}
