package com.streamr.client.authentication;

import com.google.gson.Gson;
import com.unifina.domain.security.SecUser;
import com.unifina.security.SessionToken;
import com.unifina.service.EthereumIntegrationKeyService;
import com.unifina.service.SessionService;

import java.io.IOException;
import java.nio.charset.Charset;

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

		// TODO: replace when AuthenticationMethod.LoginResponse is visible to subclass
		String jsonResponse = new Gson().toJson(sessionToken.toMap());
		return this.parse(new okio.Buffer().writeString(jsonResponse, Charset.forName("UTF-8")));

		/*
		new AuthenticationMethod.LoginResponse() {
			@Override
			String getSessionToken() {
				return sessionToken.getToken()
			}
			@Override
			Date getExpiration() {
				return sessionToken.getExpiration()
			}
		}
		*/
	}
}
