package com.unifina.service

import com.unifina.domain.security.SessionToken
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(SessionService)
@Mock(SessionToken)
class SessionServiceSpec extends Specification {

	void "should generate session token"() {
		when:
		SessionToken token = service.generateToken("address")
		def expectedExpiration = new DateTime().plusHours(SessionService.TTL_HOURS)
		then:
		token.expiration.getMillis() - expectedExpiration.getMillis() < 500
		SessionToken.count() == 1
	}
}
