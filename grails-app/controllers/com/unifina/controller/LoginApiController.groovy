package com.unifina.controller


import com.unifina.domain.SignupMethod
import com.unifina.domain.User
import com.unifina.domain.Userish
import com.unifina.security.Challenge
import com.unifina.service.*
import grails.converters.JSON

class LoginApiController {
	ChallengeService challengeService
	SessionService sessionService
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	UserService userService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def challenge(String address) {
		Challenge ch = challengeService.createChallenge(address.toLowerCase())
		render(ch.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def response(ChallengeResponseCommand cmd) {
		if (cmd.hasErrors()) {
			throw new InvalidArgumentsException(cmd.errors.getFieldErrors().collect {it.field+" expected."}.join(" "))
		}
		challengeService.checkValidChallengeResponse(cmd.challenge?.id,
			cmd.challenge?.challenge, cmd.signature.toLowerCase(), cmd.address.toLowerCase())
		User user = ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(cmd.address.toLowerCase(), SignupMethod.fromRequest(request))
		assertEnabled(user)
		SessionToken token = sessionService.generateToken(user)
		render(token.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def password(UsernamePasswordCommand cmd) {
		if (cmd.hasErrors()) {
			throw new InvalidArgumentsException(cmd.errors.getFieldErrors().collect {it.field+" expected."}.join(" "))
		}
        User user = userService.getUserFromUsernameAndPassword(cmd.username, cmd.password)
		assertEnabled(user)
		SessionToken token = sessionService.generateToken(user)
		render(token.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def apikey(ApiKeyCommand cmd) {
		if (cmd.hasErrors()) {
			throw new InvalidArgumentsException(cmd.errors.getFieldErrors().collect {it.field+" expected."}.join(" "))
		}
		// returns either a User or a Key (anonymous key)
		Userish userish = userService.getUserishFromApiKey(cmd.apiKey)
		if (userish instanceof User) {
			assertEnabled((User) userish)
		}
		SessionToken token = sessionService.generateToken(userish)
		render(token.toMap() as JSON)
	}

	private void assertEnabled(User user) {
		if (!user.enabled) {
			throw new DisabledUserException("Cannot login with disabled user")
		}
	}
}
