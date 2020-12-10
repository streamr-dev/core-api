package com.unifina.service

import com.unifina.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@TestFor(SessionService)
@Mock([User])
class SessionServiceSpec extends Specification {

	void setup() {
		service.keyValueStoreService = Mock(KeyValueStoreService)
		service.userService = Mock(UserService)
	}

	void "generateToken() should generate session token from user"() {
		User user = new User(id: 123L).save(failOnError: true, validate: false)
		when:
		SessionToken token = service.generateToken(user)
		then:
		1 * service.keyValueStoreService.setWithExpiration(_, _, _)
		token.getToken().length() == SessionService.TOKEN_LENGTH
	}

	void "generateToken() should update SecUsers lastLogin"() {
		User user = new User(id: 123L).save(failOnError: true, validate: false)
		when:
		service.generateToken(user)
		then:
		user.lastLogin != new Date(0)
	}

	void "getUserishFromToken() should return user"() {
		User user = new User(id: 123L).save(failOnError: true, validate: false)
		String token = "token"
		when:
		User retrieved = (User) service.getUserishFromToken(token)
		then:
		1 * service.keyValueStoreService.get(token) >> "User"+user.id.toString()
		retrieved.id == user.id
	}
}
