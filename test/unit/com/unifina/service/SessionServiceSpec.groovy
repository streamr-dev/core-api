package com.unifina.service

import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(SessionService)
@Mock(SecUser)
class SessionServiceSpec extends Specification {

	void "should generate session token"() {
		when:
		SecUser user = new SecUser(
			username: "username",
			password: "password",
			name: "name",
			email: "email@email.com",
			timezone: "timezone"
		).save(failOnError: true, validate: false)
		SessionToken token = service.generateToken(user)
		DateTime expectedExpiration = new DateTime().plusHours(SessionService.TTL_HOURS)
		then:
		token.getExpiration().getMillis() - expectedExpiration.getMillis() < 500
		token.getToken().length() == SessionService.TOKEN_LENGTH
	}
}
