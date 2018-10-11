package com.unifina.service

import com.unifina.security.Challenge
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ChallengeService)
class ChallengeServiceSpec extends Specification {
	String text

	void setup() {
		text = "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n"
	}

	void "should generate challenge"() {
		when:
		Challenge challenge = service.createChallenge()
		DateTime expectedExpiration = new DateTime().plusSeconds(ChallengeService.TTL_SECONDS)
		then:
		challenge.getChallenge().startsWith(text)
		new DateTime(challenge.getExpiration()).getMillis() - expectedExpiration.getMillis() < 500
		challenge.getId().length() == ChallengeService.CHALLENGE_LENGTH
	}
}
