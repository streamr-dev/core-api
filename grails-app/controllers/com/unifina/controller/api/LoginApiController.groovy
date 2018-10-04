package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.crypto.ECRecover
import com.unifina.domain.security.SessionToken
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import com.unifina.domain.security.Challenge

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class LoginApiController {

	static allowedMethods = [challenge: "POST", response: "POST"]
	def challengeService
	def sessionService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def challenge() {
		def ch = challengeService.createChallenge()
		render([id: ch.id, challenge: ch.challenge] as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def response(ChallengeResponseCommand cmd) {
		def valid = challengeService.verifyChallengeResponse(cmd.challenge.id, cmd.challenge.challenge, cmd.signature, cmd.address)
		if(!valid){
			throw new ApiException(400, 'INVALID_CHALLENGE', "challenge validation failed: "+cmd.challenge.id+" "+cmd.address)
		}else{
			SessionToken sk = sessionService.generateToken(cmd.address)
			render([
				token	: sk.token,
				expires	: sk.expiration.toString()
			] as JSON)
		}
	}
}
