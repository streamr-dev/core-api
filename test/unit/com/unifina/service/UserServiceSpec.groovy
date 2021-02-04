package com.unifina.service

import com.unifina.domain.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.validation.FieldError
import spock.lang.Specification

@TestFor(UserService)
@Mock([User, Role, UserRole, Module, Permission])
class UserServiceSpec extends Specification {

	void createData() {
		// The roles created
		["ROLE_USER", "ROLE_LIVE", "ROLE_ADMIN"].each {
			def role = new Role()
			role.authority = it
			role.save()
		}
	}

	def permissionService

	def setup() {
		permissionService = service.permissionService = Mock(PermissionService)
		service.streamService = Mock(StreamService)
		service.canvasService = Mock(CanvasService)
	}

	def "the user is created when called"() {
		when:
		createData()
		User user = service.createUser([username: "test@test.com", name:"test", enabled:true, accountLocked:false])

		then:
		User.count() == 1
		user.getAuthorities().size() == 0 // By default, user's have no roles
	}

	void "censoring errors with checkErrors() works properly"() {
		List checkedErrors

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

	def "delete user"() {
		setup:
		User user = new User()
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
