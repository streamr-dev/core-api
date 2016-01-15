package com.unifina.service

import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic
import org.apache.log4j.Logger

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.SavedSignalPath
import org.hibernate.proxy.HibernateProxyHelper
import org.springframework.validation.FieldError

import com.unifina.domain.security.Permission

class PermissionService {

	def grailsApplication
	SpringSecurityService springSecurityService
	Logger log = Logger.getLogger(PermissionService)

	final ownerPermissions = ["read", "write", "share"]

	public List<String> getPermittedOperations(SecUser user, resource) {
		if (!resource) {
			log.warn("canRead: missing resource domain object!")
			return []
		}
		if (!user?.id) {
			log.warn("canRead: missing user!")
			return []
		}

		// resource owner has all rights
		if (resource.hasProperty("user") && resource.user?.id != null && resource.user.id == user.id) {
			return ownerPermissions
		}

		// proxy objects have funky class names, e.g. com.unifina.domain.signalpath.ModulePackage_$$_jvst12_1b
		def clazz = HibernateProxyHelper.getClassWithoutInitializingProxy(resource).name
		return Permission.withCriteria {
			eq("user", user)
			eq("clazz", clazz)
			eq("longId", resource.id)		// TODO: handle stringId
		}*.operation;
	}

	/** Test if given user can read given resource instance */
	boolean canRead(SecUser user, resource, boolean logIfDenied=true) {
		def perms = getPermittedOperations(user, resource)

		if (!perms && logIfDenied) {
			log.warn("${user?.username}(id ${user?.id}) tried to read $resource without Permission!")
			if (resource?.user) {
				log.warn("||-> $resource is owned by ${resource.user.username} (id ${resource.user.id})")
			}
		}

		// any permissions imply read access
		return perms != []
		//return "read" in perms
	}

	/** Get all resources of given type that the user has read access to */
	public static <T> List<T> getAllReadable(SecUser user, Class<T> resourceClass) {
		def resourceClassName = resourceClass?.name;
		if (!resourceClassName /*|| !grailsApplication.isDomainClass(resourceClass)*/) {
			//log.warn("getAllReadable: Not a resource type: $resourceClassName")
			return []
		}

		// any permissions imply read access
		def readableIds = Permission.withCriteria {
			eq "user", user
			eq "clazz", resourceClassName
		}*.longId

		return resourceClass.withCriteria {
			or {
				// resources that specify an "owner" automatically give that user all access rights
				if (resourceClass.properties["declaredFields"].any { it.name == "user" }) {
					eq "user", user
				}
				"in" "id", readableIds
			}
		}
	}

	@Deprecated
	@CompileStatic
	boolean canAccess(Object instance, SecUser user=springSecurityService.getCurrentUser()) {
		return canRead(user, instance)
	}

	@CompileStatic
	boolean canAccess(ModulePackage modulePackage, SecUser user=springSecurityService.getCurrentUser()) {
		return canRead(user, modulePackage)
	}

	@CompileStatic
	boolean canAccess(Module module, SecUser user=springSecurityService.getCurrentUser()) {
		return canAccess(module.modulePackage, user)
		//return canAccess(ModulePackage.load(module.modulePackageId), user)
	}

	@Deprecated
	@CompileStatic 
	boolean canAccess(RunningSignalPath rsp, SecUser user=springSecurityService.getCurrentUser()) {
		// Shared RunningSignalPaths can be accessed by everyone
		return rsp?.shared || canRead(user, rsp)
	}

	@Deprecated
	@CompileStatic
	boolean canAccess(SavedSignalPath ssp, boolean isLoad, SecUser user=springSecurityService.getCurrentUser()) {
		// Examples can be read by everyone
		if (isLoad && ssp.type == SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH)
			return true
		else return canRead(user, ssp)
	}

	/**
	 * Looks up a user based on api keys. Returns null if the keys do not match a user.
	 * @param apiKey
	 * @param apiSecret
	 * @return
	 */
	SecUser getUserByApiKey(String apiKey, String apiSecret) {
		if (!apiKey || !apiSecret)
			return null
			
		SecUser user = SecUser.findByApiKey(apiKey)
		if (!user || user.apiSecret != apiSecret)
			return null
		else return user
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
