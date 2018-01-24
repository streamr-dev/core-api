package com.unifina.service

import com.unifina.domain.security.Challenge
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ChallengeService)
@Mock(Challenge)
class ChallengeServiceSpec extends Specification {
	void "should generate challenge"() {
		when:
		def challenge = service.createChallenge()
		def expected = "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n"
		then:
		challenge.challenge.startsWith(expected)
		Challenge.count() == 1
	}
}
