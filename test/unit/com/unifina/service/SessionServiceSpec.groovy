package com.unifina.service

import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.hibernate.StaleObjectStateException
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@TestFor(SessionService)
@Mock([SecUser])
class SessionServiceSpec extends Specification {
	KeyValueStoreService keyValueStoreService

	void setup() {
		keyValueStoreService = service.keyValueStoreService = Mock(KeyValueStoreService)

		// Must mock executeUpdate(String,List) because HQL is not supported in unit test GORM
		// Used in SessionService#updateUsersLoginDate()
		def secUserMock = mockFor(SecUser)
		secUserMock.demand.static.executeUpdate(1) {String s, List p->
			SecUser.get(p[1]).lastLogin = p[0]
		}
	}

	void "generateToken() should generate session token from user"() {
		SecUser user = new SecUser(id: 123L).save(failOnError: true, validate: false)
		when:
		SessionToken token = service.generateToken(user)
		then:
		1 * keyValueStoreService.setWithExpiration(_, _, _)
		token.getToken().length() == SessionService.TOKEN_LENGTH
	}

	void "generateToken() should generate session token from anonymous key"() {
		Key key = new Key(id: 123L).save(failOnError: true, validate: false)
		when:
		SessionToken token = service.generateToken(key)
		then:
		1 * keyValueStoreService.setWithExpiration(_, _, _)
		token.getToken().length() == SessionService.TOKEN_LENGTH
	}

	void "generateToken() should update SecUsers lastLogin"() {
		SecUser user = new SecUser(id: 123L).save(failOnError: true, validate: false)
		when:
		service.generateToken(user)
		then:
		user.lastLogin != new Date(0)
	}

	void "getUserishFromToken() should return user"() {
		SecUser user = new SecUser(id: 123L).save(failOnError: true, validate: false)
		String token = "token"
		when:
		SecUser retrieved = (SecUser) service.getUserishFromToken(token)
		then:
		1 * keyValueStoreService.get(token) >> "SecUser"+user.id.toString()
		retrieved.id == user.id
	}

	void "getUserishFromToken() should return anonymous key"() {
		Key key = new Key(id: 123L).save(failOnError: true, validate: false)
		String token = "token"
		when:
		Key retrieved = (Key) service.getUserishFromToken(token)
		then:
		1 * keyValueStoreService.get(token) >> "Key"+key.id.toString()
		retrieved.id == key.id
	}

}
