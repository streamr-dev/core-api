package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.security.*
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.feed.NoOpStreamListener
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder
import org.springframework.validation.FieldError
import spock.lang.Specification

@TestFor(UserService)
@Mock([Feed, Key, SecUser, SecRole, ModulePackage, SecUserSecRole, Module, Permission])
class UserServiceSpec extends Specification {

	void createData() {
		// A feed created with minimum fields required
		Feed feed = new Feed()
		feed.id = new Long(7)
		feed.name = "testFeed"
		feed.eventRecipientClass = ""
		feed.keyProviderClass = ""
		feed.messageSourceClass = ""
		feed.module = new Module()
		feed.parserClass = ""
		feed.timezone = "Europe/Minsk"
		feed.streamListenerClass = NoOpStreamListener.name
		feed.save(failOnError: true)

		// A modulePackage created with minimum fields required
		def modulePackage = new ModulePackage()
		modulePackage.id = new Long(1)
		modulePackage.name = "test"
		modulePackage.save()

		def modulePackage2 = new ModulePackage()
		modulePackage2.id = new Long(2)
		modulePackage2.name = "test2"
		modulePackage2.save()

		// The roles created
		["ROLE_USER", "ROLE_LIVE", "ROLE_ADMIN"].each {
			def role = new SecRole()
			role.authority = it
			role.save()
		}
	}

	def permissionService

	def setup() {
		defineBeans {
			passwordEncoder(PlaintextPasswordEncoder)
			springSecurityService(SpringSecurityService)
		}
		// Do some wiring that should be done automatically but for some reason is not (in unit tests)
		grailsApplication.mainContext.getBean("springSecurityService").grailsApplication = grailsApplication
		grailsApplication.mainContext.getBean("springSecurityService").passwordEncoder = grailsApplication.mainContext.getBean("passwordEncoder")
		permissionService = service.permissionService = Mock(PermissionService)
	}

    def "the user is created when called, with default roles if none supplied"() {
		when:
		createData()
		SecUser user = service.createUser([username: "test@test.com", name:"test", password: "test", timezone:"Europe/Minsk", enabled:true, accountLocked:false, passwordExpired:false])

		then:
		SecUser.count() == 1

		user.getAuthorities().size() == 2
		user.getAuthorities().toArray()[0].authority == "ROLE_USER"
		user.getAuthorities().toArray()[1].authority == "ROLE_LIVE"
	}

	def "default API key is created for user"() {
		when:
		createData()
		SecUser user = service.createUser([username: "test@test.com", name:"test", password: "test", timezone:"Europe/Minsk", enabled:true, accountLocked:false, passwordExpired:false])

		then:
		user.getKeys().size() == 1
	}

	def "if the roles, feeds and modulePackages are given, it should use them"() {
		when:
		createData()
		SecUser user = service.createUser([
			username       : "test@test.com",
			name           : "test",
			password       : "test",
			timezone       : "Europe/Minsk",
			enabled        : true,
			accountLocked  : false,
			passwordExpired: false
		],
			SecRole.findAllByAuthorityInList(["ROLE_USER"]),
			new ArrayList<Feed>(),
			ModulePackage.findAllByIdInList([new Long(1), new Long(2)])
		)

		then:
		user.getAuthorities().size() == 1
		user.getAuthorities().toArray()[0].authority == "ROLE_USER"

		1 * permissionService.get(Feed, {it.id == 1} as SecUser) >> []
		1 * permissionService.get(ModulePackage, {it.id == 1} as SecUser) >> []
		1 * permissionService.systemGrant({ it.id == 1} as SecUser, { it.id == 1} as ModulePackage)
		1 * permissionService.systemGrant({ it.id == 1} as SecUser, { it.id == 2} as ModulePackage)
    }

	def "it should fail if the default roles, feeds of modulePackages are not found"() {
		when:
		// The data has not been created
		SecUser user = service.createUser([username: "test@test.com", name: "test", password: "test", timezone: "Europe/Minsk", enabled: true, accountLocked: false, passwordExpired: false])

		then:
		thrown RuntimeException
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
}
