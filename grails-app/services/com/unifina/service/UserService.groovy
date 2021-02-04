package com.unifina.service

import com.unifina.domain.*
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError

class UserService {

	MessageSource messageSource

	GrailsApplication grailsApplication
	PermissionService permissionService
	StreamService streamService
	CanvasService canvasService

	User createUser(Map properties, List<Role> roles = null) {
		User user = new User(properties)

		// When created, the account is always enabled
		user.enabled = true

		if (!user.validate()) {
			def errors = checkErrors(user.errors.getAllErrors())
			log.warn(errors)
			def errorStrings = errors.collect { FieldError e ->
				if (e.getCode() == "unique") {
					return "Email already in use."
				} else {
					return e.toString()
				}

			}
			throw new UserCreationFailedException("Registration failed:\n" + errorStrings.join(",\n"))
		}

		if (!user.save(flush: false)) {
			log.warn("Failed to save user data: " + checkErrors(user.errors.getAllErrors()))
			throw new UserCreationFailedException()
		} else {
			// Save roles, feeds and module packages
			addRoles(user, roles)

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

	def addRoles(User user, List<Role> roles = null) {
		roles?.each { Role role ->
			new UserRole().create(user, role)
		}
	}

	def delete(User user) {
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
	List<FieldError> checkErrors(List<FieldError> errorList) {
		List<String> blackList = (List<String>) grailsApplication?.config?.grails?.exceptionresolver?.params?.exclude
		if (blackList == null) {
			blackList = Collections.emptyList()
		}
		List<FieldError> finalErrors = new ArrayList<>()
		List<FieldError> toBeCensoredList = new ArrayList<>()
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
}
