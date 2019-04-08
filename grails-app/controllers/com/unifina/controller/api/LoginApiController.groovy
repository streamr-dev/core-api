package com.unifina.controller.api

import com.unifina.api.InvalidArgumentsException
import com.unifina.domain.security.SecUser
import com.unifina.security.Challenge
import com.unifina.security.SessionToken
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.security.Userish
import com.unifina.service.ChallengeService
import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.service.SessionService
import com.unifina.service.UserService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class LoginApiController {

	static allowedMethods = [challenge: "POST", response: "POST", password: "POST", apikey: "POST"]
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
		SecUser user = ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(cmd.address)
		SessionToken token = sessionService.generateToken(user)
		render(token.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def password(UsernamePasswordCommand cmd) {
		if (cmd.hasErrors()) {
			throw new InvalidArgumentsException(cmd.errors.getFieldErrors().collect {it.field+" expected."}.join(" "))
		}
		SecUser user = userService.getUserFromUsernameAndPassword(cmd.username, cmd.password)
		SessionToken token = sessionService.generateToken(user)
		render(token.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def apikey(ApiKeyCommand cmd) {
		if (cmd.hasErrors()) {
			throw new InvalidArgumentsException(cmd.errors.getFieldErrors().collect {it.field+" expected."}.join(" "))
		}
		// returns either a SecUser or a Key (anonymous key)
		Userish userish = userService.getUserishFromApiKey(cmd.apiKey)
		SessionToken token = sessionService.generateToken(userish)
		render(token.toMap() as JSON)
	}
}
