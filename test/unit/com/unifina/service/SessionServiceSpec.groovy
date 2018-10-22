package com.unifina.service

import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@TestFor(SessionService)
@Mock([SecUser])
class SessionServiceSpec extends Specification {
	KeyValueStoreService keyValueStoreService

	void setup() {
		keyValueStoreService = service.keyValueStoreService = Mock(KeyValueStoreService)
	}

	void "generateToken() should generate session token"() {
		SecUser user = new SecUser(id: 123L).save(failOnError: true, validate: false)
		when:
		SessionToken token = service.generateToken(user)
		then:
		1 * keyValueStoreService.setWithExpiration(_, _, _)
		token.getToken().length() == SessionService.TOKEN_LENGTH
	}

	void "getUserFromToken() should return user"() {
		SecUser user = new SecUser(id: 123L).save(failOnError: true, validate: false)
		String token = "token"
		when:
		SecUser retrieved = service.getUserFromToken(token)
		then:
		1 * keyValueStoreService.get(token) >> user.id.toString()
		retrieved.id == user.id
	}
}
