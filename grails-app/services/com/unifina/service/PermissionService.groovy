package com.unifina.service

import grails.gorm.DetachedCriteria
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

import java.security.AccessControlException

class PermissionService {

	def grailsApplication
	SpringSecurityService springSecurityService
	Logger log = Logger.getLogger(PermissionService)

	final allOperations = ["read", "write", "share"]
	final ownerPermissions = allOperations

	// cascade some revocations to "higher" rights too
	//   to ensure a meaningful combination (e.g. "write" without "read" makes no sense)
	final alsoRevoke = ["read": ["write", "share"]]

	/** ownership (if applicable) is stored in each Resource as "user" attribute */
	private boolean isOwner(SecUser user, resource) {
		return resource?.hasProperty("user") &&
				user?.id != null &&
				resource.user?.id != null &&
				resource.user.id == user.id
	}

	/**
	 * In addition, owner has all access, which you must remember to test separately!
	 * @param user if null, query all users for this resource
     * @return List of Permissions that have been granted by the resource owner
     */
	public List<Permission> getNonOwnerPermissions(SecUser user, resource) {
		if (!resource) { throw new IllegalArgumentException("Missing resource!") }
		if (!grailsApplication.isDomainClass(resource.getClass())) {
			throw new IllegalArgumentException("$resource is not a Grails domain object")
		}

		def idProp = resource.hasProperty("id")
		if (idProp == null) { throw new IllegalArgumentException("$resource doesn't have an 'id' field!") }
		def hasStringId = idProp.type == String
		if (!hasStringId && idProp.type != Long) {
			throw new IllegalArgumentException("$resource doesn't have an 'id' field of type either Long or String!")
		}

		// proxy objects have funky class names, e.g. com.unifina.domain.signalpath.ModulePackage_$$_jvst12_1b
		//   hence, class.name of a proxy object won't match the class.name in database
		def clazz = HibernateProxyHelper.getClassWithoutInitializingProxy(resource).name
		return Permission.withCriteria {
			if (user != null) {
				eq("user", user)
			}
			eq("clazz", clazz)
			if (hasStringId) {
				eq("stringId", resource.id)
			} else {
				eq("longId", resource.id)
			}
		}
	}

	public List<String> getPermittedOperations(SecUser user, resource) {
		if (!user?.id) { throw new IllegalArgumentException("Missing user") }
		if (isOwner(user, resource)) { return ownerPermissions }
		return getNonOwnerPermissions(user, resource)*.operation;
	}

	/**
	 * System function, use responsibly! Check first that whoever asks canShare the resource!
	 * @param resource whose potential accessors are listed
	 * @return [secUser: ["read", "write", "share"]]
	 */
	public Map<SecUser, List<String>> getPermittedOperationsGroupedByUser(resource) {
		Map perms = getNonOwnerPermissions(null, resource)
				.groupBy { it.user }
				.collectEntries { u, ps -> [u, ps*.operation] }
		if (resource.user) { perms[resource.user] = ownerPermissions }
		return perms
	}

	/** Test if given user can read given resource instance */
	boolean canRead(SecUser user, resource) {
		if (!resource?.id) { return false; }
		// TODO: check first if resource is "public" i.e. always readable, also to null user
		if (!user?.id) { return false; }
		// any permissions imply read access
		return getPermittedOperations(user, resource) != []
		//return "read" in getPermittedOperations(user, resource)
	}

	/** Test if given user can share given resource instance, that is, grant other users read/share rights */
	boolean canShare(SecUser user, resource) {
		if (!resource?.id || !user?.id) { return false; }
		return "share" in getPermittedOperations(user, resource)
	}

	private <T> List<T> getWithCriteria(SecUser user, Class<T> resourceClass, Closure resourceFilter, Closure permissionFilter) {
		// TODO: return resources that are "public" i.e. always readable, also to null user
		if (!user) { return [] }

		def resourceClassName = resourceClass?.name;
		if (!resourceClassName) { throw new IllegalArgumentException("Missing resource class") }
		if (!grailsApplication.isDomainClass(resourceClass)) {
			throw new IllegalArgumentException("$resourceClass is not a Grails domain class")
		}

		def idProp = resourceClass.properties["declaredFields"].find { it.name == "id" }
		if (idProp == null) { throw new IllegalArgumentException("$resourceClass doesn't have an 'id' field!") }
		def hasStringId = idProp.type == String
		if (!hasStringId && idProp.type != Long) {
			throw new IllegalArgumentException("$resourceClass doesn't have an 'id' field of type either Long or String!")
		}

		// any permissions imply read access
		def perms = Permission.withCriteria {
			eq "user", user
			eq "clazz", resourceClassName

			permissionFilter.delegate = delegate
			permissionFilter()
		}
		def resourceIds = hasStringId ? perms*.stringId : perms*.longId;

		def criteria = new DetachedCriteria(resourceClass).build {
			or {
				// resources that specify an "owner" automatically give that user all access rights
				if (resourceClass.properties["declaredFields"].any { it.name == "user" }) {
					eq "user", user
				}
				// empty in-list will work with Mock but fail with SQL
				if (resourceIds.size() > 0) {
					"in" "id", resourceIds
				}
			}
		}
		return criteria.list(resourceFilter)
	}

	/** Get all resources of given type that the user has read access to */
	public <T> List<T> getAllReadable(SecUser user, Class<T> resourceClass, Closure resourceFilter = {}) {
		return getWithCriteria(user, resourceClass, resourceFilter) {}
	}

	/** Get all resources of given type that the user can grant access to for others */
	public <T> List<T> getAllShareable(SecUser user, Class<T> resourceClass, Closure resourceFilter = {}) {
		return getWithCriteria(user, resourceClass, resourceFilter) {
			eq "operation", "share"
		}
	}

	/**
	 * Grant a Permission to another SecUser to access a resource
	 * @param grantor user that has "share" rights to the resource
	 * @param resource to be shared
	 * @param target user that will receive the access
	 * @param operation "read", "write", "share"
	 * @return Permission object that was created
	 * @throws AccessControlException if grantor doesn't have the 'share' permission
	 * @throws IllegalArgumentException if trying to give resource owner "more" access permissions
     */
	public Permission grant(SecUser grantor, resource, SecUser target, String operation="read", boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		// owner already has all access, can't give "more" access
		if (isOwner(target, resource)) {
			throw new IllegalArgumentException("Can't add access permissions to $resource for owner (${target?.username}, id ${target?.id})!")
		}

		// TODO CORE-498: check grantor himself has the right he's granting (e.g. "write")
		if (canShare(grantor, resource)) {
			return systemGrant(target, resource, operation)
		} else {
			if (logIfDenied) {
				log.warn("${grantor?.username}(id ${grantor?.id}) tried to share $resource without Permission!")
				if (resource?.user) {
					log.warn("||-> $resource is owned by ${resource.user.username} (id ${resource.user.id})")
				}
			}
			throw new AccessControlException("${grantor?.username}(id ${grantor?.id}) has no 'share' permission to $resource!")
		}
	}

	/**
	 * "Internal" version of Permission granting; not done as any specific SecUser
	 * @param target user that will receive the access
	 * @param resource to be shared
	 * @param operation "read", "write", "share"
     * @return
     */
	public Permission systemGrant(SecUser target, resource, String operation="read") {
		if (operation in allOperations) {
			// proxy objects have funky class names, e.g. com.unifina.domain.signalpath.ModulePackage_$$_jvst12_1b
			//   hence, class.name of a proxy object won't match later class.name queries
			def clazz = HibernateProxyHelper.getClassWithoutInitializingProxy(resource)
			def idProp = clazz.properties["declaredFields"].find { it.name == "id" }
			if (idProp == null) { throw new IllegalArgumentException("$clazz doesn't have an 'id' field!") }
			if (idProp.type == Long) {
				return new Permission(user: target, clazz: clazz.name, longId: resource.id, operation: operation).save(flush: true, failOnError: true)
			} else if (idProp.type == String) {
				return new Permission(user: target, clazz: clazz.name, stringId: resource.id, operation: operation).save(flush: true, failOnError: true)
			} else {
				throw new IllegalArgumentException("$clazz doesn't have an 'id' field of type either Long or String!")
			}
		} else {
			throw new IllegalArgumentException("Operation should be one of " + allOperations)
		}
	}

	/**
	 * Revoke a Permission from another SecUser to access a resource
	 * @param revoker needs a "share" Permission to the resource (or else, is the owner)
	 * @param resource to be revoked from target
	 * @param target user whose Permission is revoked
	 * @param operation includes also all "higher" operations, e.g. "read" also revokes "share"
	 * @returns Permission objects that were deleted
     */
	public List<Permission> revoke(SecUser revoker, resource, SecUser target, String operation="read", boolean logIfDenied=true) throws AccessControlException {
		// can't revoke ownership
		if (isOwner(target, resource)) {
			throw new AccessControlException("Can't revoke owner's (${target?.username}, id ${target?.id}) access to $resource!")
		}

		if (canShare(revoker, resource)) {
			return systemRevoke(target, resource, operation)
		} else {
			if (logIfDenied) {
				log.warn("${revoker?.username}(id ${revoker?.id}) tried to revoke $resource without 'share' Permission!")
				if (resource?.user) {
					log.warn("||-> $resource is owned by ${resource.user.username} (id ${resource.user.id})")
				}
			}
			throw new AccessControlException("${revoker?.username}(id ${revoker?.id}) has no 'share' permission to $resource!")
		}
	}

	/**
	 * "Internal" version of Permission revocation; not done as any specific SecUser
	 * @param target user whose Permission is revoked
	 * @param resource to be revoked from target
	 * @param operation includes also all "higher" operations, e.g. "read" also revokes "share"
     * @return Permission objects that were deleted
     */
	public List<Permission> systemRevoke(SecUser target, resource, String operation="read") {
		def ret = []
		def perms = getNonOwnerPermissions(target, resource)
		def revokeOp = { String op -> perms.findAll { it.operation == op }.each {
			ret.add(it)
			it.delete()
		} }
		revokeOp operation
		alsoRevoke.get(operation).each revokeOp
		return ret
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
