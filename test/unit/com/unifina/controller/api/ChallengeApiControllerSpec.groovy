package com.unifina.controller.api

import com.unifina.domain.security.Challenge
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.service.ChallengeService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ChallengeApiController)
@Mock([SecUser, com.unifina.filters.UnifinaCoreAPIFilters, Key])
class ChallengeApiControllerSpec extends Specification {
	ChallengeService challengeService
	def setup() {
		challengeService = controller.challengeService = Mock(ChallengeService)
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
}
