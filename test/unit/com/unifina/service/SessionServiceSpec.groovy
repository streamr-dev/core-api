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
	KeyValueStoreService keyValueStoreService

	void setup() {
		keyValueStoreService = service.keyValueStoreService = Mock(KeyValueStoreService)

		// Must mock executeUpdate(String,List) because HQL is not supported in unit test GORM
		// Used in SessionService#updateUsersLoginDate()
		def userMock = mockFor(User)
		userMock.demand.static.executeUpdate(1) { String s, List p ->
			User.get(p[1]).lastLogin = p[0]
		}
	}

	void "generateToken() should generate session token from user"() {
		User user = new User(id: 123L).save(failOnError: true, validate: false)
		when:
		SessionToken token = service.generateToken(user)
		then:
		1 * keyValueStoreService.setWithExpiration(_, _, _)
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
		User retrieved = service.getUserFromToken(token)
		then:
		1 * keyValueStoreService.get(token) >> "User" + user.id.toString()
		retrieved.id == user.id
	}
}
