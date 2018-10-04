package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.domain.security.Challenge
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SessionToken
import com.unifina.service.ChallengeService
import com.unifina.service.SessionService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(LoginApiController)
@Mock([Challenge, SessionToken, SecUser, com.unifina.filters.UnifinaCoreAPIFilters, Key])
class LoginApiControllerSpec extends Specification {
	ChallengeService challengeService
	SessionService sessionService
	def setup() {
		challengeService = controller.challengeService = Mock(ChallengeService)
		sessionService = controller.sessionService = Mock(SessionService)
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

		SessionToken sk = new SessionToken(id: "id", token: "token", associatedAddress: address, expiration: new DateTime())
			.save(failOnError: true, validate: true)

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
		response.status == 200
		response.json == [
			token	: sk.token,
			expires	: sk.expiration.toString()
		]
		1 * challengeService.verifyChallengeResponse(challenge.id, challenge.challenge, signature, address) >> true
		1 * sessionService.generateToken(address) >> sk
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
