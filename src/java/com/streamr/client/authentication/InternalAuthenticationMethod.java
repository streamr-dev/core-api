package com.streamr.client.authentication;

import com.unifina.domain.SignupMethod;
import com.unifina.domain.User;
import com.unifina.service.EthereumUserService;
import com.unifina.service.SessionService;
import com.unifina.service.SessionToken;
import org.web3j.utils.Numeric;

import java.io.IOException;

public class InternalAuthenticationMethod extends EthereumAuthenticationMethod {
	private final SessionService sessionService;
	private final User user;

	public InternalAuthenticationMethod(String ethereumPrivateKey, EthereumUserService ethereumUserService, SessionService sessionService, SignupMethod signupMethod) {
		super(Numeric.prependHexPrefix(ethereumPrivateKey));
		this.user = ethereumUserService.getOrCreateFromEthereumAddress(this.getAddress(), signupMethod);
		this.sessionService = sessionService;
	}

	@Override
	protected AuthenticationMethod.LoginResponse login(String restApiUrl) throws IOException {
		final SessionToken sessionToken = sessionService.generateToken(user);
		return new AuthenticationMethod.LoginResponse(sessionToken.getToken(), sessionToken.getExpiration());
	}
}
