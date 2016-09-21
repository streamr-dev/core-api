package com.unifina.service

import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite
import org.apache.log4j.Logger
import org.hibernate.proxy.HibernateProxyHelper

import java.security.AccessControlException

/**
 * get, check, grant and revoke functions that query and control the Access Control Lists (ACLs) to resources
 *
 * Complexities handled by PermissionService:
 * 		- in addition to Permissions, "resource owner" (i.e. resource.user if exists) has all access to that resource
 * 			-> there doesn't always exist a Permission object in database for each "access right"
 * 			=> generate dummy Permission objects with id == null
 * 		- anonymous Permissions: checked, and listed for resource, but permitted resources not listed for user
 * 		- resources can be pointed with longId or stringId
 * 			=> getIdPropertyName
 * 		- Permission owners and grant/revoke targets can be SecUsers or SignupInvites
 * 			=> getUserPropertyName
 * 			-> following is supported for both: grant, revoke, systemGrant, systemRevoke, getPermissionsTo
 * 		- combinations of read/write/share (RWS) should be restricted, e.g. to disallow write without read?
 * 			=> alsoRevoke, alsoGrant
 * 			TODO: discuss... current implementation with alsoRevoke but no alsoGrant is conservative but unsymmetric
 */
class PermissionService {

	def grailsApplication
	Logger log = Logger.getLogger(PermissionService)

	// cascade some revocations to "higher" rights too
	//   to ensure a meaningful combination (e.g. WRITE without READ makes no sense)
	final alsoRevoke = [read: [Operation.WRITE, Operation.SHARE]]
	//final alsoGrant = [write: [Operation.READ], share: [Operation.READ]]


	// TODO: does WRITE imply READ?
	boolean canRead(SecUser user, resource)  { return check(user, resource, Operation.READ) }
	boolean canWrite(SecUser user, resource) { return check(user, resource, Operation.WRITE) }
	boolean canShare(SecUser user, resource) { return check(user, resource, Operation.SHARE) }

	/**
	 * @return true if user is allowed to perform given operation to resource
	 */
	public boolean check(SecUser user, resource, Operation op) {
		if (!resource?.id) { return false; }

		Class resourceClass = HibernateProxyHelper.getClassWithoutInitializingProxy(resource)
		// Make sure we are operating on an instance attached to the session
		resource = resourceClass.get(resource.id)

		if (isOwner(user, resource)) { return true; }		// owner has all access
		return hasPermission(user, resourceClass, resource.id, op)
	}

	private boolean hasPermission(user, Class resourceClass, resourceId, Operation op) {
		String idProp = getIdPropertyName(resourceClass)
		def p = Permission.withCriteria {
			or {
				eq "anonymous", true
				if (isValidUser(user)) {
					String userProp = getUserPropertyName(user)
					eq userProp, user
				}
			}
			eq "clazz", resourceClass.name
			eq idProp, resourceId
			eq "operation", op
		}
		return !p.empty
	}

	/**
	 * @return List of all Permissions granted to a specific resource
     */
	public List<Permission> getPermissionsTo(resource) {
		return getPermissionsList(resource, null, true)
	}

	/**
	 * @param user whose permissions are asked, or null for anonymous permissions only
	 * @return List of all Permissions granted to the user for the resource
	 */
	public List<Permission> getSingleUserPermissionsTo(resource, SecUser user) {
		return getPermissionsList(resource, user, false)
	}

	private List<Permission> getPermissionsList(resource, SecUser user, boolean getAllPermissions) {
		if (!resource) { throw new IllegalArgumentException("Missing resource!") }

		// proxy objects have funky class names, e.g. com.unifina.domain.signalpath.ModulePackage_$$_jvst12_1b
		//   hence, class.name of a proxy object won't match the class.name in database
		Class resourceClass = HibernateProxyHelper.getClassWithoutInitializingProxy(resource)
		String idProp = getIdPropertyName(resourceClass)

		List<Permission> perms = Permission.withCriteria {
			eq("clazz", resourceClass.name)
			eq(idProp, resource.id)
			if (!getAllPermissions) {
				or {
					eq "anonymous", true
					if (isValidUser(user)) {
						String userProp = getUserPropertyName(user)
						eq userProp, user
					}
				}
			}
		}.toList()

		// Generated non-saved "dummy permissions" for owner
		if (resource.hasProperty("user") && (getAllPermissions || resource.user == user)) {
			Permission.Operation.enumConstants.each {
				perms << new Permission(
					id: null,
					user: resource.user,
					clazz: resourceClass.name,
					operation: it,
					(idProp): resource.id
				)
			}
		}
		return perms
	}

	/**
	 * Get all resources of given type that the user has specified type of access to
	 * @throws IllegalArgumentException for bad resourceClass
	 */
	public <T> List<T> get(Class<T> resourceClass,
						   SecUser user,
						   Operation op,
						   boolean includeAnonymous, Closure resourceFilter = {}) {
		if (!includeAnonymous && !user?.id) { return [] }
		String resourceIdProp = getIdPropertyName(resourceClass)	// throws if bad resource class

		// two queries needed because type system has been violated
		//   in SQL, you could Permission p JOIN ResourceClass r ON p.(resourceIdProp)=r.id
		def perms = Permission.withCriteria {
			or {
				if (includeAnonymous) { eq "anonymous", true }
				if (user?.id) { eq "user", user }
			}
			eq "clazz", resourceClass.name
			eq "operation", op
		}

		// or-clause in criteria query should become false, and nothing should be returned
		boolean hasOwner = resourceClass.properties["declaredFields"].any { it.name == "user" }
		if (!hasOwner && perms.size() == 0) { return [] }

		resourceClass.withCriteria {
			or {
				// resources that specify an "owner" automatically give that user all access rights
				if (hasOwner) {
					eq "user", user
				}
				// empty in-list will work with Mock but fail with SQL
				if (perms.size() > 0) {
					"in" "id", perms.collect { it[resourceIdProp] }
				}
			}
			resourceFilter.delegate = delegate
			resourceFilter()
		}
	}
	/** Overload to allow leaving out the anonymous-include-flag but including the filter */
	public <T> List<T> get(Class<T> resourceClass, SecUser user, Operation op, Closure resourceFilter = {}) {
		return get(resourceClass, user, op, false, resourceFilter)
	}
	/** Convenience overload, adding a flag for public resources may look cryptic */
	public <T> List<T> getAll(Class<T> resourceClass, SecUser user, Operation op, Closure resourceFilter = {}) {
		return get(resourceClass, user, op, true, resourceFilter)
	}
	/** Overload to allow leaving out the op but including the filter */
	public <T> List<T> get(Class<T> resourceClass, SecUser user, Closure resourceFilter = {}) {
		return get(resourceClass, user, Operation.READ, false, resourceFilter)
	}
	/** Convenience overload, adding a flag for public resources may look cryptic */
	public <T> List<T> getAll(Class<T> resourceClass, SecUser user, Closure resourceFilter = {}) {
		return get(resourceClass, user, Operation.READ, true, resourceFilter)
	}

	/**
	 * Grant a Permission to another SecUser to access a resource
	 * @param grantor user that has SHARE rights to the resource
	 * @param resource to be shared
	 * @param target user that will receive the access
	 * @return Permission object that was created
	 * @throws AccessControlException if grantor doesn't have the 'share' permission
	 * @throws IllegalArgumentException if trying to give resource owner "more" access permissions
     */
	public Permission grant(SecUser grantor,
							resource,
							target,
							Operation operation=Operation.READ,
							boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		if (target instanceof SecUser && isOwner(target, resource)) {
			// owner already has all access, can't give "more" access
			throw new IllegalArgumentException("Can't grant permissions for owner of $resource!")
		}

		// TODO CORE-498: check grantor himself has the right he's granting? (e.g. "write")
		if (!canShare(grantor, resource)) {
			throwAccessControlException(grantor, resource, logIfDenied)
		}

		return systemGrant(target, resource, operation)
	}

	/**
	 * "Internal" version of Permission granting; not done as any specific SecUser
	 * @param target SecUser (or transiently SignupInvite) that will receive the access
	 * @param resource to be shared
     * @return created Permission object
     */
	public Permission systemGrant(target,
								  resource,
								  Operation operation=Operation.READ) {
		if (!resource) { throw new IllegalArgumentException("Missing resource!") }
		String userProp = getUserPropertyName(target)

		// proxy objects have funky class names, e.g. com.unifina.domain.signalpath.ModulePackage_$$_jvst12_1b
		//   hence, class.name of a proxy object won't match the class.name in database
		def resourceClass = HibernateProxyHelper.getClassWithoutInitializingProxy(resource)
		String idProp = getIdPropertyName(resourceClass)

		return new Permission(
			clazz: resourceClass.name,
			operation: operation,
			(idProp): resource.id,
			(userProp): target,
		).save(flush: true, failOnError: true)
	}

	public Permission grantAnonymousAccess(SecUser grantor,
										   resource,
										   Operation operation=Operation.READ,
										   boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		if (!canShare(grantor, resource)) {
			throwAccessControlException(grantor, resource, logIfDenied)
		}
		return systemGrantAnonymousAccess(resource, operation)
	}

	public Permission systemGrantAnonymousAccess(resource, Operation operation=Operation.READ) {
		if (!resource) { throw new IllegalArgumentException("Missing resource!") }

		def resourceClass = HibernateProxyHelper.getClassWithoutInitializingProxy(resource)
		String idProp = getIdPropertyName(resourceClass)
		return new Permission(
			clazz: resourceClass.name,
			operation: operation,
			(idProp): resource.id,
			anonymous: true
		).save(flush: true, failOnError: true)
	}

	/**
	 * Revoke a Permission from another SecUser to access a resource
	 * @param revoker needs a SHARE Permission to the resource (or else, is the owner)
	 * @param resource to be revoked from target
	 * @param target user whose Permission is revoked
	 * @param operation includes also all "higher" operations, e.g. READ also revokes SHARE
	 * @returns Permission objects that were deleted
     */
	public List<Permission> revoke(SecUser revoker,
								   resource,
								   target,
								   Operation operation=Operation.READ,
								   boolean logIfDenied=true) throws AccessControlException {
		if (target instanceof SecUser && isOwner(target, resource)) {
			throw new AccessControlException("Can't revoke owner's access to $resource!")
		}

		if (!canShare(revoker, resource)) {
			throwAccessControlException(revoker, resource, logIfDenied)
		}

		return systemRevoke(target, resource, operation)
	}

	/**
	 * "Internal" version of Permission revocation; not done as any specific SecUser
	 * @param target user whose Permission is revoked
	 * @param resource to be revoked from target
	 * @param operation includes also all "higher" operations, e.g. READ also revokes SHARE
     * @return Permission objects that were deleted
     */
	public List<Permission> systemRevoke(target,
										 resource,
										 Operation operation=Operation.READ) {
		if (!resource) { throw new IllegalArgumentException("Missing resource!") }
		String userProp = getUserPropertyName(target)

		// proxy objects have funky class names, e.g. com.unifina.domain.signalpath.ModulePackage_$$_jvst12_1b
		//   hence, class.name of a proxy object won't match the class.name in database
		def resourceClass = HibernateProxyHelper.getClassWithoutInitializingProxy(resource)
		String idProp = getIdPropertyName(resourceClass)

		return performRevoke(false, userProp, target, resourceClass.name, idProp, resource.id, operation)
	}

	public List<Permission> revokeAnonymousAccess(SecUser revoker,
												  resource,
												  Operation operation=Operation.READ,
												  boolean logIfDenied=true) throws AccessControlException {
		if (!canShare(revoker, resource)) {
			throwAccessControlException(revoker, resource, logIfDenied)
		}
		return systemRevokeAnonymousAccess(resource, operation)
	}

	public List<Permission> systemRevokeAnonymousAccess(resource, Operation operation=Operation.READ) {
		if (!resource) { throw new IllegalArgumentException("Missing resource!") }
		def resourceClass = HibernateProxyHelper.getClassWithoutInitializingProxy(resource)
		String idProp = getIdPropertyName(resourceClass)
		return performRevoke(true, userProp, target, resourceClass.name, idProp, resource.id, operation)
	}

	/**
	 * Shortcut for removing a given Permission.
	 * Doesn't need to load the resource at all if revoker has a share permission
     */
	public List<Permission> revoke(SecUser revoker, Permission p, boolean logIfDenied=true) {
		Class resourceClass = grailsApplication.getDomainClass(p.clazz)
		String idProp = getIdPropertyName(resourceClass)
		def resourceId = p[idProp]

		// if revoker has share permission, no need to load resource
		if (!hasPermission(revoker, resourceClass, resourceId, Operation.SHARE)) {
			// fall back to loading the resource to test ownership
			def resource = resourceClass.get(resourceId)
			if (!isOwner(revoker, resource)) {
				throwAccessControlException(revoker, resource, logIfDenied)
			}
		}
		return systemRevoke(p)
	}

	/** "Internal" version of revoke shortcut */
	public List<Permission> systemRevoke(Permission p) {
		if (!p) { throw new IllegalArgumentException("Missing Permission object!") }
		return performRevoke(
				p.anonymous,	// userProp and target will be ignored if anonymous==true
				p.user ? "user" : "invite",
				p.user ?: p.invite,
				p.clazz,
				p.stringId ? "stringId" : "longId",
				p.stringId ?: p.longId,
				p.operation)
	}

	/** find Permissions that will be revoked, and cascade according to alsoRevoke map */
	private List<Permission> performRevoke(boolean anonymous, String userProp, target, String clazz, String idProp, resourceId, Operation operation) {
		List<Permission> ret = []
		List<Permission> perms = Permission.withCriteria {
			if (anonymous) {
				eq("anonymous", true)
			} else {
				eq(userProp, target)
			}
			eq("clazz", clazz)
			eq(idProp, resourceId)
		}
		log.info("performRevoke: Found permissions for $clazz $resourceId: $perms")
		def revokeOp = { Operation op ->
			perms.findAll { it.operation == op }.each { Permission perm ->
				ret.add(perm)
				try {
					log.info("performRevoke: Trying to delete permission $perm.id")
					Permission.withNewTransaction {
						perm.delete(flush: true)
					}
				} catch (Throwable e) {
					// several threads could be deleting the same permission, all after first resulting in StaleObjectStateException
					// e.g. API calls "revoke write" + "revoke read" arrive so that "revoke read" comes first
					// ignoring the exception is fine; after all, the permission has been deleted
					log.warn("Caught throwable while deleting permission $perm.id: $e")
				}
			}
		}
		revokeOp(operation)
		alsoRevoke.get(operation.id).each(revokeOp)
		return ret
	}

	private throwAccessControlException(SecUser violator, resource, loggingEnabled) {
		if (loggingEnabled) {
			log.warn("${violator?.username}(id ${violator?.id}) tried to modify sharing of $resource without SHARE Permission!")
			if (resource?.hasProperty("user")) {
				log.warn("||-> $resource is owned by ${resource.user.username} (id ${resource.user.id})")
			}
		}
		throw new AccessControlException("${violator?.username}(id ${violator?.id}) has no 'share' permission to $resource!")
	}

	/**
	 * Convert signup-invitation Permissions that have been created when sharing resources to non-users
	 * @param user that was just created from sign-up invite
     * @return List of Permissions the invite had that the new user received
     */
	public List<Permission> transferInvitePermissionsTo(SecUser user) {
		// { invite { eq "username", user.username } } won't do: some invite are null => NullPointerException
		return Permission.withCriteria {
			isNotNull "invite"
		}.findAll {
			it.invite.username == user.username
		}.collect { p ->
			p.invite = null
			p.user = user
			p.save(flush: true, failOnError: true)
		}
	}

	/**
	 * Check that resourceClass is a proper resource
	 * @return name of field in Permission object that refers to the resource's id by type ("stringId" or "longId")
	 */
	private String getIdPropertyName(Class resourceClass) throws IllegalArgumentException {
		if (!resourceClass?.name) { throw new IllegalArgumentException("Missing resource class") }
		if (!grailsApplication.isDomainClass(resourceClass)) { throw new IllegalArgumentException("$resourceClass is not a Grails domain class") }

		def idProp = resourceClass.properties["declaredFields"].find { it.name == "id" }
		if (idProp == null) { throw new IllegalArgumentException("$resourceClass doesn't have an 'id' field!") }

		if (idProp.type == String) {
			return "stringId"
		} else if (idProp.type == Long) {
			return "longId"
		} else {
			throw new IllegalArgumentException("$resourceClass doesn't have an 'id' field of type either Long or String!")
		}
	}

	/**
	 * Check that grant/revoke target is a proper permission-holder
	 * @return name of field in Permission object that is the foreign-key to the user or sign-up invite
	 */
	private String getUserPropertyName(userish) throws IllegalArgumentException {
		if (!userish) {
			throw new IllegalArgumentException("Missing user!")
		} else if (userish instanceof SecUser) {
			return "user"
		} else if (userish instanceof SignupInvite) {
			return "invite"
		} else {
			throw new IllegalArgumentException("Permission holder must be a user or a sign-up-invitation!")
		}
	}

	/** null is often a valid value (but not a valid user), and means "anonymous Permissions only" */
    private boolean isValidUser(userish) {
        return userish != null && (userish instanceof SecUser || userish instanceof SignupInvite)
    }

	/** ownership (if applicable) is stored in each Resource as "user" attribute */
	private boolean isOwner(SecUser user, resource) {
		return resource?.hasProperty("user") &&
			user?.id != null &&
			resource.user?.id != null &&
			resource.user.id == user.id
	}
}
