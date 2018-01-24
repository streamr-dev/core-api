package com.unifina.controller.api

import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ChallengeApiController {

	static allowedMethods = [challenge: "POST"]
	def challengeService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def challenge() {
		def ch = challengeService.createChallenge()
		render([id: ch.id, challenge: ch.challenge] as JSON)
	}
}
