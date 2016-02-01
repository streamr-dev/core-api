package com.unifina.service

import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.UiChannel
import com.unifina.utils.IdGenerator
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GormUserDetailsService;
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder
import org.springframework.security.core.userdetails.cache.NullUserCache
import org.springframework.validation.FieldError
import spock.lang.Specification

import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser

import com.unifina.domain.security.Permission
import com.unifina.domain.dashboard.Dashboard

import java.security.AccessControlException

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(PermissionService)
@Mock([SecUser, SecRole, SecUserSecRole, Module, ModulePackage, ModulePackageUser, Permission, Dashboard, Canvas, UiChannel])
class PermissionServiceSpec extends Specification {

	SecUser me, anotherUser, stranger

	ModulePackage modPackAllowed, modPackRestricted, modPackOwned
	Module modAllowed, modRestricted, modOwned
	ModulePackageUser allowedPermission
	Permission allowedPermission2

	Dashboard dashAllowed, dashRestricted, dashOwned
	Permission dashReadPermission

	UiChannel uicAllowed, uicRestricted // UiChannels don't have an owner
	Permission uicReadPermission

    def setup() {
		
		defineBeans {
			userDetailsService(GormUserDetailsService)
			authenticationTrustResolver(AuthenticationTrustResolverImpl)
			passwordEncoder(PlaintextPasswordEncoder)
			userCache(NullUserCache)
			springSecurityService(SpringSecurityService)
		}
		
		// Do some wiring that should be done automatically but for some reason is not (in unit tests)
		grailsApplication.mainContext.getBean("springSecurityService").grailsApplication = grailsApplication
		grailsApplication.mainContext.getBean("springSecurityService").passwordEncoder = grailsApplication.mainContext.getBean("passwordEncoder")
		grailsApplication.mainContext.getBean("springSecurityService").authenticationTrustResolver = grailsApplication.mainContext.getBean("authenticationTrustResolver")
		grailsApplication.mainContext.getBean("userDetailsService").grailsApplication = grailsApplication
		
		// Users
		me = new SecUser(username: "me", password: "foo", apiKey: "apiKey", apiSecret: "apiSecret").save(validate:false)
		anotherUser = new SecUser(username: "him", password: "bar", apiKey: "anotherApiKey", apiSecret: "anotherApiSecret").save(validate:false)
		stranger = new SecUser(username: "stranger", password: "x", apiKey: "strangeApiKey", apiSecret: "strangeApiSecret").save(validate:false)

		// ModulePackages
		modPackAllowed = new ModulePackage(name:"allowed", user:anotherUser).save(validate:false)
		modPackRestricted = new ModulePackage(name:"restricted", user:anotherUser).save(validate:false)
		modPackOwned = new ModulePackage(name:"owned", user:me).save(validate:false)
		
		// Modules
		modAllowed = new Module(name:"modAllowed", modulePackage:modPackAllowed).save(validate:false)
		modRestricted = new Module(name:"modRestricted", modulePackage:modPackRestricted).save(validate:false)
		modOwned = new Module(name:"modOwned", modulePackage:modPackOwned).save(validate:false)

		// TODO: Test Permission mechanism both with a resource with longId and with stringId

		// Dashboards
		dashAllowed = new Dashboard(name:"allowed", user:anotherUser).save(validate:false)
		dashRestricted = new Dashboard(name:"restricted", user:anotherUser).save(validate:false)
		dashOwned = new Dashboard(name:"owned", user:me).save(validate:false)

		// Ui channels (have stringId, have no "user")
		def canvas = new Canvas(user: anotherUser).save(validate: false)
		uicAllowed = new UiChannel(id: "allowed_ui_channel", canvas: 1, name:"allowed")
		uicRestricted = new UiChannel(id: "restricted_ui_channel", canvas: 1, name:"restricted")
		uicAllowed.id = IdGenerator.get()
		uicRestricted.id = IdGenerator.get()
		uicAllowed.save(validate: false)
		uicRestricted.save(validate: false)

		// Set up the permission to the allowed resources
		allowedPermission = new ModulePackageUser(user:me, modulePackage:modPackAllowed).save()
		allowedPermission2 = service.grant(anotherUser, modPackAllowed, me)
		dashReadPermission = service.grant(anotherUser, dashAllowed, me)
		uicReadPermission = service.systemGrant(me, uicAllowed)
		
		// Configure SpringSecurity fields
		def userLookup = [:]
		def authority = [:]
		userLookup.userDomainClassName = SecUser.class.getName()
		userLookup.usernamePropertyName = 'username'
		userLookup.enabledPropertyName = 'enabled'
		userLookup.passwordPropertyName = 'password'
		userLookup.authoritiesPropertyName = 'authorities'
		userLookup.accountExpiredPropertyName = 'accountExpired'
		userLookup.accountLockedPropertyName = 'accountLocked'
		userLookup.passwordExpiredPropertyName = 'passwordExpired'
		userLookup.authorityJoinClassName = 'SecUserSecRole'
		authority.className = 'SecRole'
		authority.nameField = 'authority'
		
		SpringSecurityUtils.securityConfig = [userLookup:userLookup, authority:authority]
		SpringSecurityUtils.setApplication(grailsApplication)
    }

    def cleanup() {
    }

	void "test setup"() {
		expect:
		SecUser.count()==3
		ModulePackage.count()==3
		Module.count()==3
		ModulePackageUser.count()==1
		Module.findByModulePackage(modPackAllowed)==modAllowed
		ModulePackage.findAllByUser(anotherUser).size()==2
		ModulePackage.findAllByUser(me).size()==1
		ModulePackageUser.findByUserAndModulePackage(me, modPackAllowed)==allowedPermission

		Permission.count()==3
		
		SpringSecurityUtils.doWithAuth("me") {
			grailsApplication.mainContext.getBean("springSecurityService").currentUser == me
		}
	}

	void "access granted to permitted Dashboard"() {
		expect:
		service.canRead(me, dashAllowed)
	}

	void "access denied to non-permitted Dashboard"() {
		expect:
		!service.canRead(me, dashRestricted)
	}

	void "access granted to own Dashboard"() {
		expect:
		service.canRead(me, dashOwned)
	}

	void "non-permitted third-parties have no access to resources"() {
		expect:
		!service.canRead(stranger, dashAllowed)
		!service.canRead(stranger, dashRestricted)
		!service.canRead(stranger, dashOwned)
	}

	void "canRead returns false on bad inputs"() {
		expect:
		!service.canRead(null, dashAllowed)
		!service.canRead(me, new Dashboard())
		!service.canRead(me, null)
	}

	void "retrieve all readable Dashboards correctly"() {
		expect:
		service.getAllReadable(me, Dashboard) == [dashOwned, dashAllowed]
		service.getAllReadable(anotherUser, Dashboard) == [dashAllowed, dashRestricted]
		service.getAllReadable(stranger, Dashboard) == []
	}

	void "retrieve all readable UiChannels correctly"() {
		expect:
		service.getAllReadable(me, UiChannel) == [uicAllowed]
		service.getAllReadable(stranger, UiChannel) == []
	}

	void "grant and revoke work for UiChannels"() {
		when:
		service.systemGrant(anotherUser, uicRestricted)
		then:
		service.getAllReadable(me, UiChannel) == [uicAllowed]
		service.getAllReadable(anotherUser, UiChannel) == [uicRestricted]

		when:
		service.systemRevoke(anotherUser, uicRestricted)
		then:
		service.getAllReadable(me, UiChannel) == [uicAllowed]
		service.getAllReadable(anotherUser, UiChannel) == []
	}

	void "getAllReadable returns throws IllegalArgumentException on invalid resource"() {
		when:
		service.getAllReadable(me, java.lang.Object)
		then:
		thrown IllegalArgumentException

		when:
		service.getAllReadable(me, null)
		then:
		thrown IllegalArgumentException
	}

	void "getAllReadable returns public resources on bad/null user"() {
		expect:
		service.getAllReadable(new SecUser(), Dashboard) == []
		service.getAllReadable(null, Dashboard) == []
	}

	void "getAllReadable closure filtering works as expected"() {
		expect:
		service.getAllReadable(me, Dashboard) { like("name", "%ll%") } == [dashAllowed]
	}

	void "getAllShareable closure filtering works as expected"() {
		expect:
		service.getAllShareable(me, Dashboard) == [dashOwned]
		service.getAllShareable(me, Dashboard) { like("name", "%ll%") } == []
	}

	void "granting and revoking read rights"() {
		when:
		service.grant(me, dashOwned, stranger)
		then:
		service.getAllReadable(stranger, Dashboard) == [dashOwned]

		when:
		service.revoke(me, dashOwned, stranger)
		then:
		service.getAllReadable(stranger, Dashboard) == []
	}

	void "grant and revoke throw for non-'share'-access users"() {
		when:
		service.grant(me, dashAllowed, stranger)
		then:
		thrown AccessControlException

		when:
		service.revoke(stranger, dashRestricted, me)
		then:
		thrown AccessControlException

		when:
		service.grant(anotherUser, dashAllowed, me, "share")
		service.revoke(me, dashAllowed, anotherUser)
		then: "Y U try to revoke owner's access?! That should never be generated from the UI!"
		thrown AccessControlException
	}

	void "sharing read rights to others"() {
		when:
		service.grant(me, dashOwned, stranger, "share")
		then:
		service.getAllReadable(stranger, Dashboard) == [dashOwned]
		service.getAllShareable(stranger, Dashboard) == [dashOwned]

		expect:
		!(dashOwned in service.getAllReadable(anotherUser, Dashboard))

		when: "stranger shares read access"
		service.grant(stranger, dashOwned, anotherUser)
		then:
		dashOwned in service.getAllReadable(anotherUser, Dashboard)
		!(dashOwned in service.getAllShareable(anotherUser, Dashboard))

		when:
		service.revoke(stranger, dashOwned, anotherUser)
		then:
		!(dashOwned in service.getAllReadable(anotherUser, Dashboard))

		when: "of course, it's silly to revoke 'share' access since it might already been re-shared..."
		service.revoke(me, dashOwned, stranger)
		service.grant(stranger, dashOwned, anotherUser)
		then:
		thrown AccessControlException
	}

	void "revocation is granular"() {
		setup:
		service.grant(me, dashOwned, stranger, "read")
		service.grant(me, dashOwned, stranger, "share")
		when:
		service.revoke(me, dashOwned, stranger, "share")
		then: "only 'share' access is revoked"
		service.getAllReadable(stranger, Dashboard) == [dashOwned]
		service.getAllShareable(stranger, Dashboard) == []
	}

	void "default revocation is all access"() {
		setup:
		service.grant(me, dashOwned, stranger, "read")
		service.grant(me, dashOwned, stranger, "share")
		when:
		service.revoke(me, dashOwned, stranger)
		then: "by default, revoke all access"
		service.getAllReadable(stranger, Dashboard) == []
		service.getAllShareable(stranger, Dashboard) == []
	}

	void "granting works (roughly) idempotently"() {
		expect:
		service.getAllReadable(stranger, Dashboard) == []
		when:
		service.grant(me, dashOwned, stranger)
		then: "now you see it..."
		service.getAllReadable(stranger, Dashboard) == [dashOwned]
		when:
		service.grant(me, dashOwned, stranger)
		service.grant(me, dashOwned, stranger)
		service.grant(me, dashOwned, stranger)
		service.revoke(me, dashOwned, stranger)
		then: "now you don't."
		service.getAllReadable(stranger, Dashboard) == []
	}

	//----------------------------------
	// (soon to be) deprecated canAccess methods
	// once these are dumped, also SpringSecurityUtils dependency is gone from tester; no need to defineBeans

	void "access denied when no user logged in"() {
		expect:
		!service.canAccess(modPackOwned)
		!service.canAccess(modOwned)
	}

	void "access granted to owned module and package"() {
		expect:
		SpringSecurityUtils.doWithAuth("me") {
			service.canAccess(modPackOwned)
		}
		SpringSecurityUtils.doWithAuth("me") {
			service.canAccess(modOwned)
		}
	}

	void "access granted to permitted module and package"() {
		expect:
		SpringSecurityUtils.doWithAuth("me") {
			service.canAccess(modPackAllowed)
		}
		SpringSecurityUtils.doWithAuth("me") {
			service.canAccess(modAllowed)
		}
	}

	void "access denied to restricted module and package"() {
		expect:
		SpringSecurityUtils.doWithAuth("me") {
			!service.canAccess(modPackRestricted)
		}
		SpringSecurityUtils.doWithAuth("me") {
			!service.canAccess(modRestricted)
		}
	}

	void "granting access to restricted object based supplied user"() {
		expect:
		service.canAccess(modPackOwned, me)
		!service.canAccess(modPackRestricted, me)
		!service.canAccess(modPackOwned, anotherUser)
	}

	//----------------------------------
	// Completely unrelated set of methods from UnifinaSecurityService

	void "looking up a user based on correct api key"() {
		when:
		def user = service.getUserByApiKey("apiKey")

		then:
		user.username == me.username
	}
	
	void "looking up a user with incorrect api key"() {
		when:
		def user = service.getUserByApiKey("wrong api key")
		
		then:
		!user
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
