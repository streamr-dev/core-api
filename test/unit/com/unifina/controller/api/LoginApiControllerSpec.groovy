package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.domain.security.Challenge
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import com.unifina.service.ChallengeService
import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.service.SessionService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(LoginApiController)
@Mock([Challenge, SecUser, com.unifina.filters.UnifinaCoreAPIFilters, Key])
class LoginApiControllerSpec extends Specification {
	ChallengeService challengeService
	SessionService sessionService
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	def setup() {
		challengeService = controller.challengeService = Mock(ChallengeService)
		sessionService = controller.sessionService = Mock(SessionService)
		ethereumIntegrationKeyService = controller.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		def me = new SecUser().save(failOnError: true, validate: false)
		Key key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)
	}

	void "should generate challenge"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/login/challenge"
		request.method = "POST"
		withFilters(action: "challenge") {
			controller.challenge()
		}

		then:
		response.status == 200
		response.json == [
				id       : null,
				challenge: "challenge 123"
		]
		1 * challengeService.createChallenge() >> new Challenge(id: "123", challenge: "challenge 123")
	}

	def "response to challenge should pass"() {
		String address = "address"
		String signature = "signature"

		Challenge challenge = new Challenge(id: "id", challenge: "challenge").save(failOnError: true, validate: true)

		SecUser user = new SecUser(
			username: "username",
			password: "password",
			name: "name",
			email: "email@email.com",
			timezone: "timezone"
		).save(failOnError: true, validate: false)

		SessionToken sk = new SessionToken(64, user, 3)

		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/login/response"
		request.method = "POST"
		request.JSON = [
			challenge: [
				id       : challenge.id,
				challenge: challenge.challenge
			],
			signature: signature,
			address  : address
		]
		withFilters(action: "response") {
			controller.response()
		}

		then:
		1 * challengeService.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address) >> true
		1 * ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(address) >> user
		1 * sessionService.generateToken(user) >> sk
		response.status == 200
		response.json == [
			token	: sk.getToken(),
			expires	: sk.getExpiration().toString()
		]
	}

	def "response to challenge should fail"() {
		String address = "address"
		String signature = "signature"

		Challenge challenge = new Challenge(id: "id", challenge: "challenge").save(failOnError: true, validate: true)

		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/login/response"
		request.method = "POST"
		request.JSON = [
			challenge: [
				id       : challenge.id,
				challenge: challenge.challenge
			],
			signature: signature,
			address  : address
		]
		withFilters(action: "response") {
			controller.response()
		}

		then:
		thrown ApiException
		1 * challengeService.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address) >> false
	}
}
