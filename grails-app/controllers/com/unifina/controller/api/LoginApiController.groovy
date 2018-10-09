package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class LoginApiController {

	static allowedMethods = [challenge: "POST", response: "POST"]
	def challengeService
	def sessionService
	def ethereumIntegrationKeyService
	def userService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def challenge() {
		def ch = challengeService.createChallenge()
		render([id: ch.id, challenge: ch.challenge] as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def response(ChallengeResponseCommand cmd) {
		if(cmd.challenge==null){
			throw new ApiException(400, 'INVALID_CHALLENGE', "challenge validation failed")
		}
		boolean valid = challengeService.verifyChallengeResponse(cmd.challenge.id,
			cmd.challenge.challenge, cmd.signature.toLowerCase(), cmd.address.toLowerCase())
		if(!valid){
			throw new ApiException(400, 'INVALID_CHALLENGE', "challenge-based login failed")
		}else{
			SecUser user = ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(cmd.address)
			SessionToken sk = sessionService.generateToken(user)
			render([
				token	: sk.getToken(),
				expires	: sk.getExpiration().toString()
			] as JSON)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def password(UsernamePasswordCommand cmd) {
		SecUser user = userService.getUserFromUsernameAndPassword(cmd.username, cmd.password)
		if(user==null){
			throw new ApiException(400, 'INVALID_USERNAME_OR_PASSWORD', "password-based login failed")
		}
		SessionToken sk = sessionService.generateToken(user)
		render([
			token	: sk.getToken(),
			expires	: sk.getExpiration().toString()
		] as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(LoginCommand cmd) {
		switch(cmd.method) {
			case LoginCommand.Method.ETHEREUM: redirect(action: challenge())
			case LoginCommand.Method.PASSWORD: //TODO: add password-based login
			default: redirect(action: challenge())
		}
	}
}
