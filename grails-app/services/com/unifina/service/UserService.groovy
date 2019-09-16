package com.unifina.service

import com.unifina.api.DisabledUserException
import com.unifina.api.InvalidAPIKeyException
import com.unifina.api.InvalidUsernameAndPasswordException
import com.unifina.api.NotFoundException
import com.unifina.domain.ExampleType
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.exceptions.UserCreationFailedException
import com.unifina.security.Userish
import grails.plugin.springsecurity.SpringSecurityService
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.MessageSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.validation.FieldError

class UserService {

	MessageSource messageSource

	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService
	PermissionService permissionService
	StreamService streamService
	CanvasService canvasService

	SecUser createUser(Map properties, List<SecRole> roles = null, List<ModulePackage> packages = null) {
		def secConf = grailsApplication.config.grails.plugin.springsecurity
		ClassLoader cl = this.getClass().getClassLoader()
		SecUser user = cl.loadClass(secConf.userLookup.userDomainClassName).newInstance(properties)

		// Encode the password
		if (user.password == null) {
			throw new UserCreationFailedException("The password is empty!")
		}
		user.password = springSecurityService.encodePassword(user.password)

		// When created, the account is always enabled
		user.enabled = true

		if (!user.validate()) {
			def errors = checkErrors(user.errors.getAllErrors())
			log.warn(errors)
			def errorStrings = errors.collect { e ->
				if (e.getCode() == "unique") {
					"Email already in use."
				} else {
					e.toString()
				}

			}
			throw new UserCreationFailedException("Registration failed:\n" + errorStrings.join(",\n"))
		}

		// Users must have at least one API key
		user.addToKeys(new Key(name: "Default"))

		if (!user.save(flush: true)) {
			log.warn("Failed to save user data: " + checkErrors(user.errors.getAllErrors()))
			throw new UserCreationFailedException()
		} else {
			// Save roles, feeds and module packages
			addRoles(user, roles)
			setModulePackages(user, packages ?: [])

			// Transfer permissions that were attached to sign-up invitation before user existed
			permissionService.transferInvitePermissionsTo(user)
		}

		try {
			List<Canvas> canvasExamples = Canvas.createCriteria().list {
				ne("exampleType", ExampleType.NOT_SET)
			}
			canvasService.addExampleCanvases(user, canvasExamples)
		} catch (RuntimeException e) {
			log.error("error while adding example canvases: ", e)
		}

		try {
			List<Stream> streamExamples = Stream.createCriteria().list {
				eq("exampleType", ExampleType.SHARE)
			}
			streamService.addExampleStreams(user, streamExamples)
		} catch (RuntimeException e) {
			log.error("error while adding example streams: ", e)
		}

		log.info("Created user for " + user.username)

		return user
	}

	def addRoles(user, List<SecRole> roles = null) {
		def secConf = grailsApplication.config.grails.plugin.springsecurity
		ClassLoader cl = this.getClass().getClassLoader()

		def userRoleClass = cl.loadClass(secConf.userLookup.authorityJoinClassName)
		def roleClass = cl.loadClass(secConf.authority.className)

		if (roles == null) {
			roles = roleClass.findAllByAuthorityInList(secConf.ui.register.defaultRoleNames)
			if (roles.size() != secConf.ui.register.defaultRoleNames.size()) {
				throw new RuntimeException("Roles not found: " + secConf.ui.register.defaultRoleNames)
			}
		}

		roles.each { role ->
			userRoleClass.create user, role
		}
	}

	/** Adds/removes ModulePackage read permissions so that user's permissions match given ones */
	def setModulePackages(user, List<ModulePackage> packages) {
		List<ModulePackage> existing = permissionService.get(ModulePackage, user)
		packages.findAll { !existing.contains(it) }.each { permissionService.systemGrant(user, it) }
		existing.findAll { !packages.contains(it) }.each { permissionService.systemRevoke(user, it) }
		return packages
	}

	def passwordValidator = { String password, command ->
		// Check password score
		if (command.password != null && command.password.size() < 8) {
			return ['command.password.error.length', 8]
		}
	}

	def password2Validator = { value, command ->
		if (command.password != command.password2) {
			return 'command.password2.error.mismatch'
		}
	}

	def delete(SecUser user) {
		if (user == null) {
			throw new NotFoundException("user not found", "User", null)
		}
		user.enabled = false
		user.save(validate: true)
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
		List<String> blackList = (List<String>) grailsApplication?.config?.grails?.exceptionresolver?.params?.exclude
		if (blackList == null) {
			blackList = Collections.emptyList();
		}
		List<FieldError> finalErrors = new ArrayList<>()
		List<FieldError> toBeCensoredList = new ArrayList<>();
		errorList.each {
			if (blackList.contains(it.getField())) {
				toBeCensoredList.add(it)
			} else {
				finalErrors.add(it)
			}
		}
		toBeCensoredList.each {
			List arguments = Arrays.asList(it.getArguments())
			int index = arguments.indexOf(it.getRejectedValue())
			if (index >= 0 && index < arguments.size()) {
				arguments.set(index, "***")
			}
			FieldError fieldError = new FieldError(
				it.getObjectName(), it.getField(), "***", it.isBindingFailure(),
				it.getCodes(), arguments.toArray(), it.getDefaultMessage()
			)
			finalErrors.add(fieldError)
		}
		return finalErrors
	}

	List beautifyErrors(List<FieldError> errorList) {
		checkErrors(errorList).collect { FieldError it ->
			messageSource.getMessage(it, null)
		}
	}

	SecUser getUserFromUsernameAndPassword(String username, String password) throws InvalidUsernameAndPasswordException {
		PasswordEncoder encoder = new BCryptPasswordEncoder()
		SecUser user = SecUser.findByUsername(username)
		if (user == null) {
			throw new InvalidUsernameAndPasswordException("Invalid username or password")
		}
		String dbHash = user.password
		if (encoder.matches(password, dbHash)) {
			return user
		}else {
			throw new InvalidUsernameAndPasswordException("Invalid username or password")
		}
	}

	Userish getUserishFromApiKey(String apiKey) throws InvalidAPIKeyException {
		Key key = Key.get(apiKey)
		if (!key) {
			throw new InvalidAPIKeyException("Invalid API key")
		}
		if (key.user) { // is a 'real' user
			return key.user
		}
		return key // is an anonymous key
	}
}
