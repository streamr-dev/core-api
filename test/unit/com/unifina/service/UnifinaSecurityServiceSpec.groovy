package com.unifina.service

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

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(UnifinaSecurityService)
@Mock([SecUser, SecRole, SecUserSecRole, Module, ModulePackage, ModulePackageUser])
class UnifinaSecurityServiceSpec extends Specification {

	SecUser me
	SecUser anotherUser
	ModulePackage allowed
	ModulePackage restricted
	ModulePackage owned
	Module modAllowed
	Module modRestricted
	Module modOwned
	ModulePackageUser allowedPermission
	
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
		me = new SecUser(username: "me", password: "foo", apiKey: "apiKey")
		anotherUser = new SecUser(username: "him", password: "bar", apiKey: "anotherApiKey")
		me.save(validate:false)
		anotherUser.save(validate:false)
		
		// ModulePackages
		allowed = new ModulePackage(name:"allowed", user:anotherUser).save(validate:false)
		restricted = new ModulePackage(name:"restricted", user:anotherUser).save(validate:false)
		owned = new ModulePackage(name:"owned", user:me).save(validate:false)
		
		// Modules
		modAllowed = new Module(name:"modAllowed", modulePackage:allowed).save(validate:false)
		modRestricted = new Module(name:"modRestricted", modulePackage:restricted).save(validate:false)
		modOwned = new Module(name:"modOwned", modulePackage:owned).save(validate:false)
		
		// Set up the permission to the allowed package
		allowedPermission = new ModulePackageUser(user:me, modulePackage:allowed).save()
		
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
		SecUser.count()==2
		ModulePackage.count()==3
		Module.count()==3
		ModulePackageUser.count()==1
		Module.findByModulePackage(allowed)==modAllowed
		ModulePackage.findAllByUser(anotherUser).size()==2
		ModulePackage.findAllByUser(me).size()==1
		ModulePackageUser.findByUserAndModulePackage(me, allowed)==allowedPermission
		
		SpringSecurityUtils.doWithAuth("me") {
			grailsApplication.mainContext.getBean("springSecurityService").currentUser == me
		}
	}
	
    void "access denied when no user logged in"() {
		expect:
		!service.canAccess(owned)
		!service.canAccess(modOwned)
    }
	
	void "access granted to owned module and package"() {
		expect:
		SpringSecurityUtils.doWithAuth("me") {
			service.canAccess(owned)
		}
		SpringSecurityUtils.doWithAuth("me") {
			service.canAccess(modOwned)
		}
	}
	
	void "access granted to permitted module and package"() {
		expect:
		SpringSecurityUtils.doWithAuth("me") {
			service.canAccess(allowed)
		}
		SpringSecurityUtils.doWithAuth("me") {
			service.canAccess(modAllowed)
		}
	}
	
	void "access denied to restricted module and package"() {
		expect:
		SpringSecurityUtils.doWithAuth("me") {
			!service.canAccess(restricted)
		}
		SpringSecurityUtils.doWithAuth("me") {
			!service.canAccess(modRestricted)
		}
	}
	
	void "looking up a user based on correct api keys"() {
		when:
		def user = service.getUserByApiKey("apiKey")
		
		then:
		user.username == me.username
	}
	
	void "looking up a user with incorrect api keys"() {
		when:
		def user = service.getUserByApiKey("incorrectApiKey")
		
		then:
		!user
		
		when:
		user = service.getUserByApiKey("wrong api key")
		
		then:
		!user
	}
	
	void "granting access to restricted object based supplied user"() {
		expect:
		service.canAccess(owned, me)
		!service.canAccess(restricted,me)
		!service.canAccess(owned, anotherUser)
	}

	void "censoring errors with checkErrors() works properly"(){
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
