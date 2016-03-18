package com.unifina.service

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.springframework.validation.FieldError

class UnifinaSecurityService {

	def grailsApplication
	SpringSecurityService springSecurityService
	Logger log = Logger.getLogger(UnifinaSecurityService)

	/**
	 * Checks if the given user has access to the given instance.
	 * If no user is provided, the user identified by api keys or the current logged in user (as returned by springSecurityService.currentUser) is used
	 * (in that order of precedence).
	 * 
	 * Access to instance is granted if the instance has a field called "user",
	 * and the user id it points to equals the user id being checked against.
	 * 
	 * @param instance
	 * @param user
	 * @return
	 */
	private boolean checkUser(instance, SecUser user=springSecurityService.getCurrentUser(), boolean logIfDenied=true) {
		if (!instance) {
			log.warn("checkUser: domain object instance is null, denying access for user ${user?.id}")
			return false
		}
		
		// Is this a protected instance?
		if (instance.hasProperty("user") && instance.user?.id!=null) {
			boolean result = instance.user.id == user?.id
			if (!result && logIfDenied) {
				log.warn("User ${user?.id} tried to access $instance owned by user $instance.user.id!")
			}
			return result
		}
		// TODO: For unprotected instances, return true. Safe?
		else return true
	}

	@CompileStatic
	boolean canAccess(Object instance, SecUser user=springSecurityService.getCurrentUser()) {
		return checkUser(instance, user) 
	}

	@CompileStatic
	boolean canAccess(Module module, SecUser user=springSecurityService.getCurrentUser()) {
		return canAccess(module.modulePackage, user)
	}
	
	@CompileStatic
	boolean canAccess(ModulePackage modulePackage, SecUser user=springSecurityService.getCurrentUser()) {
		// Everyone who has been granted access to a ModulePackage can access it
		return checkModulePackageAccess(modulePackage, user) || checkUser(modulePackage, user)
	}
	
	@CompileStatic 
	boolean canAccess(Canvas canvas, SecUser user=springSecurityService.getCurrentUser()) {
		return canvas?.shared || checkUser(canvas, user) // Shared Canvas can be accessed by everyone
	}
	
	@CompileStatic
	boolean canAccess(Canvas canvas, boolean isLoad, SecUser user=springSecurityService.getCurrentUser()) {
		// Examples can be read by everyone
		if (isLoad && (canvas.example || canvas.shared)) {
			return true
		} else {
			return canAccess(canvas, user)
		}
	}
	
	private boolean checkModulePackageAccess(ModulePackage modulePackage, SecUser user=springSecurityService.getCurrentUser()) {
		ModulePackageUser mpu = ModulePackageUser.findByUserAndModulePackage(user, modulePackage)
		return mpu != null
	}
	
	/**
	 * Looks up a user based on api key. Returns null if the keys do not match a user.
	 * @param apiKey
	 * @return
	 */
	SecUser getUserByApiKey(String apiKey) {
		if (!apiKey)
			return null
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
