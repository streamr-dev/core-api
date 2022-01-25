package com.streamr.client.authentication;

import com.streamr.core.domain.SignupMethod;
import com.streamr.core.domain.User;
import com.streamr.core.service.EthereumUserService;
import com.streamr.core.service.SessionService;
import com.streamr.core.service.SessionToken;
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
