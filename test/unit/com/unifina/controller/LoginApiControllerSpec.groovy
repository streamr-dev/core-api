package com.unifina.controller

import com.unifina.domain.SignupMethod
import com.unifina.domain.User
import com.unifina.service.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(LoginApiController)
@Mock([User])
class LoginApiControllerSpec extends ControllerSpecification {
	ChallengeService challengeService
	SessionService sessionService
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	User me

	def setup() {
		me = new User().save(failOnError: true, validate: false)
		challengeService = controller.challengeService = Mock(ChallengeService)
		sessionService = controller.sessionService = Mock(SessionService)
		ethereumIntegrationKeyService = controller.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
	}

	void "should generate challenge"() {
		Challenge challenge = new Challenge("id", "challenge", challengeService.TTL_SECONDS)
		String address = "some-address"
		when:
		params.address = address
		request.method = "POST"
		authenticatedAs(me) { controller.challenge() }

		then:
		response.status == 200
		response.json == challenge.toMap()
		1 * challengeService.createChallenge(address) >> challenge
	}

	def "response to challenge should pass"() {
		String address = "0x1234567890123456789012345678901234567890"
		String signature = "signature"

		Challenge challenge = new Challenge("id", "challenge", challengeService.TTL_SECONDS)

		User user = new User(
			username: "username",
			name: "name",
			email: "email@email.com",
			timezone: "timezone"
		).save(failOnError: true, validate: false)

		SessionToken token = new SessionToken(64, user, 3)

		when:
		request.method = "POST"
		request.JSON = [
			challenge: [
				id       : challenge.getId(),
				challenge: challenge.getChallenge()
			],
			signature: signature,
			address  : address
		]
		authenticatedAs(me) { controller.response() }

		then:
		1 * challengeService.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		1 * ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(address, SignupMethod.API) >> user
		1 * sessionService.generateToken(user) >> token
		response.status == 200
		response.json == token.toMap()
	}

	def "response to challenge should fail"() {
		String address = "0x1234567890123456789012345678901234567890"
		String signature = "signature"

		Challenge challenge = new Challenge("id", "challenge", challengeService.TTL_SECONDS)

		when:
		request.method = "POST"
		request.JSON = [
			challenge: [
				id       : challenge.getId(),
				challenge: challenge.getChallenge()
			],
			signature: signature,
			address  : address
		]
		authenticatedAs(me) { controller.response() }

		then:
		thrown ChallengeVerificationFailedException
		1 * challengeService.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address) >> { throw new ChallengeVerificationFailedException() }
	}

	def "response to challenge should fail if disabled user"() {
		String address = "0x1234567890123456789012345678901234567890"
		String signature = "signature"

		Challenge challenge = new Challenge("id", "challenge", challengeService.TTL_SECONDS)

		User user = new User(
			enabled: false
		).save(failOnError: true, validate: false)

		when:
		request.method = "POST"
		request.JSON = [
			challenge: [
				id       : challenge.getId(),
				challenge: challenge.getChallenge()
			],
			signature: signature,
			address  : address
		]
		authenticatedAs(me) { controller.response() }

		then:
		1 * challengeService.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		1 * ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(address, SignupMethod.API) >> user
		thrown DisabledUserException
	}
}
