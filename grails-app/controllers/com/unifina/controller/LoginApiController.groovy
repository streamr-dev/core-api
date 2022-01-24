package com.unifina.controller

import com.unifina.domain.SignupMethod
import com.unifina.domain.User
import com.unifina.service.*
import grails.converters.JSON

class LoginApiController {
	ChallengeService challengeService
	SessionService sessionService
	EthereumUserService ethereumUserService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def challenge(String address) {
		Challenge ch = challengeService.createChallenge(address.toLowerCase())
		render(ch.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def response(ChallengeResponseCommand cmd) {
		if (cmd.hasErrors()) {
			throw new InvalidArgumentsException(cmd.errors.getFieldErrors().collect { it.field + " expected." }.join(" "))
		}
		challengeService.checkValidChallengeResponse(cmd.challenge?.id,
			cmd.challenge?.challenge, cmd.signature.toLowerCase(), cmd.address.toLowerCase())
		String origin = request.getHeader("Origin")
		SignupMethod signupMethod = SignupMethod.fromOriginURL(origin)
		User user = ethereumUserService.getOrCreateFromEthereumAddress(cmd.address.toLowerCase(), signupMethod)
		assertEnabled(user)
		SessionToken token = sessionService.generateToken(user)
		render(token.toMap() as JSON)
	}

	private void assertEnabled(User user) {
		if (!user.enabled) {
			throw new DisabledUserException("Cannot login with disabled user")
		}
	}
}
