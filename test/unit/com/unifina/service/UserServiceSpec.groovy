package com.unifina.service

import com.unifina.api.InvalidUsernameAndPasswordException
import com.unifina.api.NotFoundException
import com.unifina.controller.api.UnitTestPasswordEncoder
import com.unifina.domain.security.*
import com.unifina.domain.signalpath.Module
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.validation.FieldError
import spock.lang.Specification

@TestFor(UserService)
@Mock([Key, SecUser, SecRole, SecUserSecRole, Module, Permission])
class UserServiceSpec extends Specification {

	void createData() {
		// The roles created
		["ROLE_USER", "ROLE_LIVE", "ROLE_ADMIN"].each {
			def role = new SecRole()
			role.authority = it
			role.save()
		}
	}

	def permissionService

	def setup() {
		service.passwordEncoder = new UnitTestPasswordEncoder()
		permissionService = service.permissionService = Mock(PermissionService)
		service.streamService = Mock(StreamService)
		service.canvasService = Mock(CanvasService)
	}

	def "the user is created when called"() {
		when:
		createData()
		SecUser user = service.createUser([username: "test@test.com", name:"test", password: "test", enabled:true, accountLocked:false, passwordExpired:false])

		then:
		SecUser.count() == 1
		user.getAuthorities().size() == 0 // By default, user's have no roles
	}

	def "default API key is created for user"() {
		when:
		createData()
		SecUser user = service.createUser([username: "test@test.com", name:"test", password: "test", enabled:true, accountLocked:false, passwordExpired:false])

		then:
		user.getKeys().size() == 1
	}

	void "censoring errors with checkErrors() works properly"() {
		List checkedErrors
		service.grailsApplication.config.grails.exceptionresolver.params.exclude = ["password"]

		when: "given list of fieldErrors"
		List<FieldError> errorList = new ArrayList<>()
		errorList.add(new FieldError(
			this.getClass().name, 'password', 'rejectedPassword', false, null, ['null', 'null', 'rejectedPassword'].toArray(), null
		))
		errorList.add(new FieldError(
			this.getClass().name, 'username', 'rejectedUsername', false, null, ['null', 'null', 'rejectedUsername'].toArray(), null
		))
		checkedErrors = service.checkErrors(errorList)

		then: "the rejected password is hidden but the rejected username is not"
		checkedErrors.get(0).getField() == "username"
		checkedErrors.get(0).getRejectedValue() == "rejectedUsername"
		checkedErrors.get(0).getArguments() == ['null', 'null', 'rejectedUsername']

		checkedErrors.get(1).getField() == "password"
		checkedErrors.get(1).getRejectedValue() == "***"
		checkedErrors.get(1).getArguments() == ['null', 'null', '***']
	}

	def "should find user from both username and password"() {
		String username = "username"
		String password = "password"
		String hashedPassword = service.passwordEncoder.encodePassword(password)
		new SecUser(username: username, password: hashedPassword).save(failOnError: true, validate: false)
		when:
		SecUser retrievedUser = service.getUserFromUsernameAndPassword(username, password)
		then:
		retrievedUser != null
		retrievedUser.username == username
	}

	def "should throw if wrong password"() {
		String username = "username"
		String password = "password"
		String wrongPassword = "wrong"
		String hashedPassword = service.passwordEncoder.encodePassword(password)
		new SecUser(username: username, password: hashedPassword).save(failOnError: true, validate: false)
		when:
		service.getUserFromUsernameAndPassword(username, wrongPassword)
		then:
		thrown(InvalidUsernameAndPasswordException)
	}

	def "should find user from api key"() {
		SecUser user = new SecUser(username: "username", password: "password").save(failOnError: true, validate: false)
		Key key = new Key(name: "key", user: user)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)

		when:
		SecUser retrievedUser = (SecUser) service.getUserishFromApiKey(key.id)
		then:
		retrievedUser != null
		retrievedUser.username == user.username
	}

	def "should find anonymous key from api key"() {
		Key key = new Key(id: "myApiKey").save(failOnError: true, validate: false)

		when:
		Key retrievedKey = (Key) service.getUserishFromApiKey(key.id)
		then:
		retrievedKey != null
		retrievedKey.id == retrievedKey.id
	}

	def "delete user"() {
		setup:
		SecUser user = new SecUser()
		user.id = 1

		when:
		service.delete(user)

		then:
		user.enabled == false
	}

	def "delete user validates parameters"() {
		when:
		service.delete(null)

		then:
		thrown NotFoundException
	}
}
