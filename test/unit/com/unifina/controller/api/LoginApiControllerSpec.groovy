package com.unifina.controller.api

import com.unifina.FilterMockingSpecification
import com.unifina.api.ApiException
import com.unifina.security.Challenge
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import com.unifina.service.ChallengeService
import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.service.SessionService
import com.unifina.service.UserService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(LoginApiController)
@Mock([SecUser, Key])
class LoginApiControllerSpec extends FilterMockingSpecification {
	ChallengeService challengeService
	SessionService sessionService
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	UserService userService
	DateFormat df
	SecUser me

	def setup() {
		me = new SecUser().save(failOnError: true, validate: false)
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
		df.setTimeZone(TimeZone.getTimeZone("UTC"))
		challengeService = controller.challengeService = Mock(ChallengeService)
		sessionService = controller.sessionService = Mock(SessionService)
		ethereumIntegrationKeyService = controller.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		userService = controller.userService = Mock(UserService)
	}

	void "should generate challenge"() {
		Challenge challenge = new Challenge("id", "challenge", challengeService.TTL_SECONDS)

		when:
		request.method = "POST"
		authenticatedAs(me) { controller.challenge() }

		then:
		response.status == 200
		response.json == challenge.toMap()
		1 * challengeService.createChallenge() >> challenge
	}

	def "response to challenge should pass"() {
		String address = "address"
		String signature = "signature"

		Challenge challenge = new Challenge("id", "challenge", challengeService.TTL_SECONDS)

		SecUser user = new SecUser(
			username: "username",
			password: "password",
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
		1 * challengeService.verifyChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address) >> true
		1 * ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(address) >> user
		1 * sessionService.generateToken(user) >> token
		response.status == 200
		response.json == token.toMap()
	}

	def "response to challenge should fail"() {
		String address = "address"
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
		thrown ApiException
		1 * challengeService.verifyChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address) >> false
	}

	def "password-based login should pass"() {
		SecUser user = new SecUser(
			username: "username",
			password: "password"
		).save(failOnError: true, validate: false)

		SessionToken token = new SessionToken(64, user, 3)

		when:
		request.method = "POST"
		request.JSON = [
			username: user.username,
			password: user.password
		]
		authenticatedAs(me) { controller.password() }

		then:
		1 * userService.getUserFromUsernameAndPassword(user.username, user.password) >> user
		1 * sessionService.generateToken(user) >> token
		response.status == 200
		response.json == token.toMap()
	}

	def "password-based login should fail"() {
		String username = "username"
		String password = "password"

		when:
		request.method = "POST"
		request.JSON = [
			username: username,
			password: password
		]
		authenticatedAs(me) { controller.password() }

		then:
		thrown ApiException
		1 * userService.getUserFromUsernameAndPassword(username, password) >> null
	}

	def "apikey-based login should pass"() {
		SecUser user = new SecUser(
			username: "username",
			password: "password"
		).save(failOnError: true, validate: false)

		String apiKey = "apiKey"

		SessionToken token = new SessionToken(64, user, 3)

		when:
		request.method = "POST"
		request.JSON = [
			apiKey: apiKey
		]
		authenticatedAs(me) { controller.apikey() }

		then:
		1 * userService.getUserFromApiKey(apiKey) >> user
		1 * sessionService.generateToken(user) >> token
		response.status == 200
		response.json == token.toMap()
	}

	def "apikey-based login should fail"() {
		String apiKey = "apiKey"

		when:
		request.method = "POST"
		request.JSON = [
			apiKey: apiKey
		]
		authenticatedAs(me) { controller.apikey() }

		then:
		1 * userService.getUserFromApiKey(apiKey) >> null
		thrown ApiException
	}
}
