package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.user.UserCreationFailedException
import org.springframework.validation.FieldError

class UserService {
    
	def grailsApplication
    def springSecurityService
	def permissionService
	
    def createUser(Map properties, List<SecRole> roles=null, List<Feed> feeds=null, List<ModulePackage> packages=null) {
        def secConf = grailsApplication.config.grails.plugin.springsecurity
        ClassLoader cl = this.getClass().getClassLoader()
        SecUser user = cl.loadClass(secConf.userLookup.userDomainClassName).newInstance(properties)

        // Encode the password
        if(user.password == null)
            throw new UserCreationFailedException("The password is empty!")
        user.password = springSecurityService.encodePassword(user.password)

        // When created, the account is always enabled
        user.enabled = true
        
        if (!user.validate()) {
            log.warn(checkErrors(user.errors.getAllErrors()))
            throw new UserCreationFailedException("Registration user validation failed: "+checkErrors(user.errors.getAllErrors()))
        }

        if (!user.save(flush:true)) {
            log.warn("Failed to save user data: "+checkErrors(user.errors.getAllErrors()))
            throw new UserCreationFailedException()
        } else {
            // Save roles, feeds and module packages
            addRoles(user, roles)
            setFeeds(user, feeds)
            setModulePackages(user, packages)
			// Transfer permissions that were attached to sign-up invitation before user existed
			permissionService.transferInvitePermissionsTo(user)
        }
        log.info("Created user for "+user.username)
        
        return user
    }

    def addRoles(user, List<SecRole> roles=null) {
        def secConf = grailsApplication.config.grails.plugin.springsecurity
        ClassLoader cl = this.getClass().getClassLoader()

        def userRoleClass = cl.loadClass(secConf.userLookup.authorityJoinClassName)
        def roleClass = cl.loadClass(secConf.authority.className)

        if(roles == null) {
            roles = roleClass.findAllByAuthorityInList(secConf.ui.register.defaultRoleNames)
            if (roles.size() != secConf.ui.register.defaultRoleNames.size())
                throw new RuntimeException("Roles not found: "+secConf.ui.register.defaultRoleNames)
        }

        roles.each { role ->
            userRoleClass.create user, role
        }
    }

	/** Adds/removes Feed read permissions so that user's permissions match given ones, or defaults if null given */
    def setFeeds(user, List<Feed> feeds=null) {
        if(feeds == null) {
            feeds = Feed.findAllByIdInList(grailsApplication.config.streamr.user.defaultFeeds.collect {it.longValue()})
            if (feeds.size() != grailsApplication.config.streamr.user.defaultFeeds.size())
                throw new RuntimeException("Feeds not found: "+grailsApplication.config.streamr.user.defaultFeeds)
        }

		List<Feed> existing = permissionService.get(Feed, user)
        feeds.findAll { !existing.contains(it) }.each { permissionService.systemGrant(user, it) }
		existing.findAll { !feeds.contains(it) }.each { permissionService.systemRevoke(user, it) }
		return feeds
    }

	/** Adds/removes ModulePackage read permissions so that user's permissions match given ones, or defaults if null given */
	def setModulePackages(user, List<ModulePackage> packages =null) {
        if (packages == null) {
            packages = ModulePackage.findAllByIdInList(grailsApplication.config.streamr.user.defaultModulePackages.collect {it.longValue()})
            if(packages.size() != grailsApplication.config.streamr.user.defaultModulePackages.size())
                throw new RuntimeException("ModulePackages not found: "+grailsApplication.config.streamr.user.defaultModulePackages)
        }

		List<ModulePackage> existing = permissionService.get(ModulePackage, user)
		packages.findAll { !existing.contains(it) }.each { permissionService.systemGrant(user, it) }
		existing.findAll { !packages.contains(it) }.each { permissionService.systemRevoke(user, it) }
		return packages
    }


	/**
	 * Looks up a user based on api key.
	 * @returns SecUser user, or null if the keys do not match a user.
	 */
	SecUser getUserByApiKey(String apiKey) {
		if (!apiKey) { return null }
		return SecUser.findByApiKey(apiKey)
	}

	def passwordValidator = { String password, command ->
		// Check password score
		if (command.pwdStrength < 1) {
			return ['command.password.error.strength']
		}
	}

	def password2Validator = { value, command ->
		if (command.password != command.password2) {
			return 'command.password2.error.mismatch'
		}
	}

	/**
	 * Checks if the errors list contains any fields whose values may not be logged
	 * as plaintext (passwords etc.). The excluded fields are read from
	 * grails.exceptionresolver.params.exclude config key.
	 *
	 * If any excluded fields are found, their field values are replaced with "***".
	 * @param errorList
	 * @return
	 */
	List checkErrors(List<FieldError> errorList) {
		List<String> blackList = (List<String>) grailsApplication.config.grails.exceptionresolver.params.exclude
		if (blackList == null) {
			blackList = Collections.emptyList();
		}
		List<FieldError> finalErrors = new ArrayList<>()
		List<FieldError> toBeCensoredList = new ArrayList<>();
		errorList.each {
			if(blackList.contains(it.getField()))
				toBeCensoredList.add(it)
			else
				finalErrors.add(it)
		}
		toBeCensoredList.each {
			List arguments = Arrays.asList(it.getArguments())
			int index = arguments.indexOf(it.getRejectedValue())
			arguments.set(index, "***")
			FieldError fieldError = new FieldError(
					it.getObjectName(), it.getField(), "***", it.isBindingFailure(), it.getCodes(), arguments.toArray(), it.getDefaultMessage()
			)
			finalErrors.add(fieldError)
		}
		return finalErrors
	}
}
