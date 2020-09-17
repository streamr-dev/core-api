package com.unifina.controller

import com.unifina.ControllerSpecification
import com.unifina.api.*
import com.unifina.domain.Key
import com.unifina.domain.SignupMethod
import com.unifina.domain.User
import com.unifina.security.Challenge
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
@Mock([User, Key])
class LoginApiControllerSpec extends ControllerSpecification {
	ChallengeService challengeService
	SessionService sessionService
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	UserService userService
	DateFormat df
    User me

	def setup() {
		me = new User().save(failOnError: true, validate: false)
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
		df.setTimeZone(TimeZone.getTimeZone("UTC"))
		challengeService = controller.challengeService = Mock(ChallengeService)
		sessionService = controller.sessionService = Mock(SessionService)
		ethereumIntegrationKeyService = controller.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		userService = controller.userService = Mock(UserService)
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
		String address = "address"
		String signature = "signature"

		Challenge challenge = new Challenge("id", "challenge", challengeService.TTL_SECONDS)

		User user = new User(
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
		1 * challengeService.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address)
		1 * ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(address, SignupMethod.API) >> user
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
		thrown ChallengeVerificationFailedException
		1 * challengeService.checkValidChallengeResponse(challenge.getId(), challenge.getChallenge(), signature, address) >> { throw new ChallengeVerificationFailedException() }
	}

	def "response to challenge should fail if disabled user"() {
		String address = "address"
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

	def "password-based login should pass"() {
		User user = new User(
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
		1 * userService.getUserFromUsernameAndPassword(username, password) >> { throw new InvalidUsernameAndPasswordException() }
		thrown InvalidUsernameAndPasswordException
	}

	def "password-based login should fail if disabled user"() {
		User user = new User(
			username: "username",
			password: "password",
			enabled: false,
		).save(failOnError: true, validate: false)

		when:
		request.method = "POST"
		request.JSON = [
			username: user.username,
			password: user.password
		]
		authenticatedAs(me) { controller.password() }

		then:
		1 * userService.getUserFromUsernameAndPassword(user.username, user.password) >> user
		thrown DisabledUserException
	}

	def "apikey-based login should pass"() {
		User user = new User(
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
		1 * userService.getUserishFromApiKey(apiKey) >> user
		1 * sessionService.generateToken(user) >> token
		response.status == 200
		response.json == token.toMap()
	}

	def "apikey-based login for anonymous key should pass"() {
		Key key = new Key(
			id: "apiKey",
		).save(failOnError: true, validate: false)

		String apiKey = "apiKey"

		SessionToken token = new SessionToken(64, key, 3)

		when:
		request.method = "POST"
		request.JSON = [
			apiKey: apiKey
		]
		authenticatedAs(me) { controller.apikey() }

		then:
		1 * userService.getUserishFromApiKey(apiKey) >> key
		1 * sessionService.generateToken(key) >> token
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
		1 * userService.getUserishFromApiKey(apiKey) >> { throw new InvalidAPIKeyException() }
		thrown InvalidAPIKeyException
	}

	def "apikey-based login should fail if disabled user"() {
		User user = new User(
			enabled: false,
		).save(failOnError: true, validate: false)

		String apiKey = "apiKey"

		when:
		request.method = "POST"
		request.JSON = [
			apiKey: apiKey
		]
		authenticatedAs(me) { controller.apikey() }

		then:
		1 * userService.getUserishFromApiKey(apiKey) >> user
		thrown DisabledUserException
	}

	def "apikey-based login should return 400 if no api key provided"() {
		when:
		request.method = "POST"
		request.JSON = [
			wrongfield: "apiKey"
		]
		authenticatedAs(me) { controller.apikey() }

		then:
		thrown InvalidArgumentsException
	}

	def "password-based login should return 400 if no username or password provided"() {
		when:
		request.method = "POST"
		request.JSON = [
			wrongfield: "password"
		]
		authenticatedAs(me) { controller.apikey() }

		then:
		thrown InvalidArgumentsException
	}
}
