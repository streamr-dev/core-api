package com.unifina.service

import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.Canvas
import grails.gorm.DetachedCriteria
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic
import org.apache.log4j.Logger

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import org.hibernate.proxy.HibernateProxyHelper
import org.springframework.validation.FieldError

import com.unifina.domain.security.Permission

import java.security.AccessControlException

/**
 * grant and revoke functions that ensure the Access Control Lists (ACLs) made up of Permissions are valid
 *
 * Complexities:
 * 		- in addition to Permissions, "resource owner" (i.e. resource.user if exists) has all access to that resource
 * 			=> there doesn't always exist a Permission object in database for each "access right"
 * 			=> hence functions like getNonOwnerPermissions vs getPermittedOperations (also for the owner)
 * 		- resources can be pointed with longId or stringId
 * 		- Permission owners and grant/revoke targets can be SecUsers or SignupInvites
 * 			=> following is supported for both: grant, revoke, systemGrant, systemRevoke, getPermissionsTo
 * 		- combinations of read/write/share (RWS) should be restricted, e.g. to disallow write without read?
 * 			TODO: discuss... current implementation with alsoRevoke but no alsoGrant is conservative but lopsided
 */
class PermissionService {

	def grailsApplication
	SpringSecurityService springSecurityService
	Logger log = Logger.getLogger(PermissionService)

	public final List<String> allOperations = ["read", "write", "share"]
	public final List<String> ownerPermissions = allOperations
	public List<String> getAllOperations() { return allOperations }

	// cascade some revocations to "higher" rights too
	//   to ensure a meaningful combination (e.g. "write" without "read" makes no sense)
	final alsoRevoke = ["read": ["write", "share"]]
	//final alsoGrant = ["write": ["read"], "share": ["read"]]

	/**
	 * Execute a closure body using a specific resource
	 * @param action Closure that takes three arguments: resourceClassName, resourceId, boolean hasStringId
	 */
	private useResource(resource, Closure action) {
		if (!resource) { throw new IllegalArgumentException("Missing resource!") }
		if (!grailsApplication.isDomainClass(resource.getClass())) {
			throw new IllegalArgumentException("$resource is not a Grails domain object")
		}

		def idProp = resource.hasProperty("id")
		if (idProp == null) { throw new IllegalArgumentException("$resource doesn't have an 'id' field!") }
		boolean hasStringId = idProp.type == String
		if (!hasStringId && idProp.type != Long) {
			throw new IllegalArgumentException("$resource doesn't have an 'id' field of type either Long or String!")
		}

		// proxy objects have funky class names, e.g. com.unifina.domain.signalpath.ModulePackage_$$_jvst12_1b
		//   hence, class.name of a proxy object won't match the class.name in database
		def clazz = HibernateProxyHelper.getClassWithoutInitializingProxy(resource).name

		action(clazz, resource.id, hasStringId)
	}

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
	private List<Permission> getNonOwnerPermissions(user, resource) {
		useResource(resource) { clazz, id, idIsString ->
			Permission.withCriteria {
				if (user != null) {
					if (user instanceof SecUser) {
						eq("user", user)
					} else if (user instanceof SignupInvite) {
						eq("invite", user)
					} else {
						throw new IllegalArgumentException("Permission owner must be a user or a sign-up-invitation!")
					}
				}
				eq("clazz", clazz)
				if (idIsString) {
					eq("stringId", id)
				} else {
					eq("longId", id)
				}
			}
		}
	}

	/**
     * @return List of operations (e.g. "read") that are permitted for given user to given resource
     */
	public List<String> getPermittedOperations(SecUser user, resource) {
		if (!user?.id) { throw new IllegalArgumentException("Missing user") }
		if (isOwner(user, resource)) { return ownerPermissions }
		return getNonOwnerPermissions(user, resource)*.operation;
	}

	/**
	 * @return List of all Permissions granted to a specific resource
     */
	public List<Permission> getPermissionsTo(resource) {
		useResource(resource) { clazz, id, idIsString ->
			List<Permission> perms = getNonOwnerPermissions(null, resource).toList()
			// Generated non-saved "dummy permissions"
			if (resource.hasProperty("user")) {
				ownerPermissions.each {
					perms.add(idIsString ?
						new Permission(id: null, user: resource.user, clazz: clazz, operation: it, stringId: resource.id) :
						new Permission(id: null, user: resource.user, clazz: clazz, operation: it, longId: resource.id))
				}
			}
			perms
		}
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
	public Permission grant(SecUser grantor, resource, target, String operation="read", boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		// owner already has all access, can't give "more" access
		if (target instanceof SecUser && isOwner(target, resource)) {
			throw new IllegalArgumentException("Can't add access permissions to $resource for owner (${target?.username}, id ${target?.id})!")
		}

		// TODO CORE-498: check grantor himself has the right he's granting (e.g. "write")
		if (canShare(grantor, resource)) {
			return systemGrant(target, resource, operation)
		} else {
			if (logIfDenied) {
				log.warn("${grantor?.username}(id ${grantor?.id}) tried to share $resource without Permission!")
				if (resource?.hasProperty("user")) {
					log.warn("||-> $resource is owned by ${resource.user.username} (id ${resource.user.id})")
				}
			}
			throw new AccessControlException("${grantor?.username}(id ${grantor?.id}) has no 'share' permission to $resource!")
		}
	}

	/**
	 * "Internal" version of Permission granting; not done as any specific SecUser
	 * @param target SecUser (or transiently SignupInvite) that will receive the access
	 * @param resource to be shared
	 * @param operation "read", "write", "share"
     * @return
     */
	public Permission systemGrant(target, resource, String operation="read") {
		if (!(operation in allOperations)) {
			throw new IllegalArgumentException("Operation should be one of " + allOperations)
		}
		def targetIsInvite = target instanceof SignupInvite
		if (!targetIsInvite && !(target instanceof SecUser)) {
			throw new IllegalArgumentException("Permission grant target must be a user or a sign-up-invitation!")
		}

		// proxy objects have funky class names, e.g. com.unifina.domain.signalpath.ModulePackage_$$_jvst12_1b
		//   hence, class.name of a proxy object won't match later class.name queries
		def clazz = HibernateProxyHelper.getClassWithoutInitializingProxy(resource)
		def idProp = clazz.properties["declaredFields"].find { it.name == "id" }
		if (idProp == null) { throw new IllegalArgumentException("$clazz doesn't have an 'id' field!") }
		if (idProp.type == Long) {
			if (targetIsInvite) {
				return new Permission(invite: target, clazz: clazz.name, longId: resource.id, stringId: null, operation: operation).save(flush: true, failOnError: true)
			} else {
				return new Permission(user: target, clazz: clazz.name, longId: resource.id, stringId: null, operation: operation).save(flush: true, failOnError: true)
			}
		} else if (idProp.type == String) {
			if (targetIsInvite) {
				return new Permission(invite: target, clazz: clazz.name, stringId: resource.id, operation: operation).save(flush: true, failOnError: true)
			} else {
				return new Permission(user: target, clazz: clazz.name, stringId: resource.id, operation: operation).save(flush: true, failOnError: true)
			}
		} else {
			throw new IllegalArgumentException("$clazz doesn't have an 'id' field of type either Long or String!")
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
	public List<Permission> revoke(SecUser revoker, resource, target, String operation="read", boolean logIfDenied=true) throws AccessControlException {
		// can't revoke ownership
		if (target instanceof SecUser && isOwner(target, resource)) {
			throw new AccessControlException("Can't revoke owner's (${target?.username}, id ${target?.id}) access to $resource!")
		}

		if (canShare(revoker, resource)) {
			return systemRevoke(target, resource, operation)
		} else {
			if (logIfDenied) {
				log.warn("${revoker?.username}(id ${revoker?.id}) tried to revoke $resource without 'share' Permission!")
				if (resource?.hasProperty("user")) {
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
	public List<Permission> systemRevoke(target, resource, String operation="read") {
		def targetIsInvite = target instanceof SignupInvite
		if (!targetIsInvite && !(target instanceof SecUser)) {
			throw new IllegalArgumentException("Permission grant target must be a user or a sign-up-invitation!")
		}

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
	boolean canAccess(Canvas canvas, SecUser user=springSecurityService.getCurrentUser()) {
		// Shared Canvas can be accessed by everyone
		return canvas?.shared || canRead(user, canvas)
	}

	@Deprecated
	@CompileStatic
	boolean canAccess(Canvas canvas, boolean isLoad, SecUser user=springSecurityService.getCurrentUser()) {
		// Examples can be read by everyone
		return isLoad && canvas?.example || canRead(user, canvas)
	}
}
