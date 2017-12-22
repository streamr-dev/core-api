package com.unifina.service

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.*
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.security.Userish
import groovy.transform.CompileStatic

import java.security.AccessControlException

/**
 * get, check, grant and revoke functions that query and control the Access Control Lists (ACLs) to resources
 *
 * Complexities handled by PermissionService:
 * 		- in addition to Permissions, "resource owner" (i.e. resource.user if exists) has all access to that resource
 * 			-> there doesn't always exist a Permission object in database for each "access right"
 * 			=> generate dummy Permission objects with id == null
 * 		- anonymous Permissions: checked, and listed for resource, but permitted resources not listed for user
 * 		- Permission owners and grant/revoke targets can be SecUsers or SignupInvites
 * 			=> getUserPropertyName
 * 			-> following is supported for both: grant, revoke, systemGrant, systemRevoke, getPermissionsTo
 * 		- combinations of read/write/share (RWS) should be restricted, e.g. to disallow write without read?
 * 			=> alsoRevoke, alsoGrant
 * 			TODO: discuss... current implementation with alsoRevoke but no alsoGrant is conservative but unsymmetric
 */
class PermissionService {
	// Cascade revocations to "higher" rights to ensure meaningful combinations (e.g. WRITE without READ makes no sense)
	private static final ALSO_REVOKE = [read: [Operation.WRITE, Operation.SHARE]]

	/**
	 * Check whether user is allowed to read a resource
	 */
	@CompileStatic
	boolean canRead(Userish userish, resource)  {
		return check(userish, resource, Operation.READ)
	}

	/**
	 * Check whether user is allowed to write a resource
	 */

	@CompileStatic
	boolean canWrite(Userish userish, resource) {
		return check(userish, resource, Operation.WRITE)
	}

	/**
	 * Check whether user is allowed to share a resource
	 */
	@CompileStatic
	boolean canShare(Userish userish, resource) {
		return check(userish, resource, Operation.SHARE)
	}

	/**
	 * Check whether user is allowed to perform given operation on a resource
	 */
	boolean check(Userish userish, resource, Operation op) {
		return resource?.id != null && (isOwner(userish, resource) || hasPermission(userish, resource, op))
	}

	/**
	 * List all Permissions granted on a resource
	 */
	List<Permission> getPermissionsTo(resource) {
		String resourceProp = getResourcePropertyName(resource)
		List<Permission> permissions = Permission.findAllWhere([(resourceProp): resource])
		return permissions + generateDummyOwnerPermissions(resource)
	}

	/**
	 * List all Permissions granted to a Userish on a resource
	 * @param userish is a SecUser, Key, SignupInvite or null (anonymous)
	 */
	List<Permission> getPermissionsTo(resource, Userish userish) {
		userish = userish?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		List<Permission> permissions = Permission.withCriteria {
			eq(resourceProp, resource)
			or {
				eq("anonymous", true)
				if (isNotNullAndIdNotNull(userish)) {
					String userProp = getUserPropertyName(userish)
					eq(userProp, userish)
				}
			}
		}.toList()

		// Generate non-saved "dummy permissions" for owner
		if (isOwner(userish, resource)) {
			permissions.addAll(generateDummyOwnerPermissions(resource))
		}
		return permissions
	}

	/** Overload to allow leaving out the anonymous-include-flag but including the filter */
	@CompileStatic
	<T> List<T> get(Class<T> resourceClass, Userish userish, Operation op, Closure resourceFilter = {}) {
		return get(resourceClass, userish, op, false, resourceFilter)
	}

	/** Convenience overload, adding a flag for public resources may look cryptic */
	@CompileStatic
	<T> List<T> getAll(Class<T> resourceClass, Userish userish, Operation op, Closure resourceFilter = {}) {
		return get(resourceClass, userish, op, true, resourceFilter)
	}

	/** Overload to allow leaving out the op but including the filter */
	@CompileStatic
	<T> List<T> get(Class<T> resourceClass, Userish userish, Closure resourceFilter = {}) {
		return get(resourceClass, userish, Operation.READ, false, resourceFilter)
	}

	/** Convenience overload, adding a flag for public resources may look cryptic */
	@CompileStatic
	<T> List<T> getAll(Class<T> resourceClass, Userish userish, Closure resourceFilter = {}) {
		return get(resourceClass, userish, Operation.READ, true, resourceFilter)
	}

	/**
	 * Get all resources of given type that the user has specified permission for
	 */
	def <T> List<T> get(Class<T> resourceClass,
						Userish userish,
						Operation op,
						boolean includeAnonymous,
						Closure resourceFilter = {}) {
		if (!includeAnonymous && !userish?.id) {
			return []
		}

		userish = userish?.resolveToUserish()
		String resourceProp = getResourcePropertyNameFromClass(resourceClass)

		// two queries needed because type system has been violated
		//   in SQL, you could Permission p JOIN ResourceClass r ON p.(idProp)=r.id
		def permissions = Permission.withCriteria {
			isNotNull(resourceProp)
			eq("operation", op)
			or {
				if (includeAnonymous) {
					eq("anonymous", true)
				}
				if (isNotNullAndIdNotNull(userish)) {
					String userProp = getUserPropertyName(userish)
					eq(userProp, userish)
				}
			}
		}

		// or-clause in criteria query should become false, and nothing should be returned
		boolean hasOwner = resourceClass.properties["declaredFields"].any { it.name == "user" }
		if (!hasOwner && permissions.isEmpty()) {
			return []
		} else {
			return resourceClass.withCriteria {
				or {
					SecUser user = userish?.resolveToSecUser()
					// resources that specify an "owner" automatically give that user all access rights
					if (hasOwner && user) {
						eq "user", user
					}
					// empty in-list will work with Mock but fail with SQL
					if (!permissions.isEmpty()) {
						"in" "id", permissions.collect { it[resourceProp].id }
					}
				}
				resourceFilter.delegate = delegate
				resourceFilter()
			}
		}
	}

	/**
	 * As a SecUser, attempt to grant Permission to another Userish to access a resource
	 * @param grantor user attempting to grant Permission (needs SHARE permission or must be owner)
	 * @param resource to be given permission on
	 * @param target Userish to be given permission to
	 * @return Permission if permission was successfully granted
	 * @throws AccessControlException if grantor doesn't have 'share' permission on resource
	 * @throws IllegalArgumentException if trying to give resource owner "more" access permissions
     */
	@CompileStatic
	Permission grant(SecUser grantor,
					 resource,
					 Userish target,
					 Operation operation=Operation.READ,
					 boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		// Owner already has all access (can't give "more" access)
		if (isOwner(target, resource)) {
			throw new IllegalArgumentException("Can't grant permissions for owner of $resource.")
		}

		// TODO CORE-498: check grantor himself has the right he's granting? (e.g. "write")
		if (!canShare(grantor, resource)) {
			throwAccessControlException(grantor, resource, logIfDenied)
		}

		return systemGrant(target, resource, operation)
	}

	/**
	 * Grant Permission to a Userish (as sudo/system)
	 * @param target Userish that will receive the access
	 * @param resource to be given permission on
     * @return granted permission
     */
	@CompileStatic
	Permission systemGrant(Userish target, resource, Operation operation=Operation.READ) {
		target = target.resolveToUserish()
		String userProp = getUserPropertyName(target)
		String resourceProp = getResourcePropertyName(resource)

		return new Permission(
			(resourceProp): resource,
			(userProp): target,
			operation: operation,
		).save(flush: true, failOnError: true)
	}

	@CompileStatic
	Permission grantAnonymousAccess(SecUser grantor,
									resource,
									Operation operation=Operation.READ,
									boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		if (!canShare(grantor, resource)) {
			throwAccessControlException(grantor, resource, logIfDenied)
		}
		return systemGrantAnonymousAccess(resource, operation)
	}

	Permission systemGrantAnonymousAccess(resource, Operation operation=Operation.READ) {
		String resourceProp = getResourcePropertyName(resource)
		return new Permission(
			(resourceProp): resource,
			operation: operation,
			anonymous: true
		).save(flush: true, failOnError: true)
	}

	/**
	 * As a SecUser, revoke a Permission from a Userish
	 * @param revoker user attempting to revoke permission (needs SHARE permission or must be owner)
	 * @param resource to be revoked from target
	 * @param target Userish user whose Permission is revoked
	 * @param operation or access level to be revoked (cascade to "higher" operations, e.g. READ also revokes SHARE)
	 * @returns Permissions that were deleted
     */
	@CompileStatic
	List<Permission> revoke(SecUser revoker,
							resource,
							Userish target,
							Operation operation=Operation.READ,
							boolean logIfDenied=true) throws AccessControlException {
		if (isOwner(target, resource)) {
			throw new AccessControlException("Can't revoke owner's access to $resource!")
		}

		if (!canShare(revoker, resource)) {
			throwAccessControlException(revoker, resource, logIfDenied)
		}

		return systemRevoke(target, resource, operation)
	}

	/**
	 * Revoke a Permission from a Userish (as sudo/system)
	 * @param target Userish whose Permission is revoked
	 * @param resource to be revoked from target
	 * @param operation or access level to be revoked (cascade to "higher" operations, e.g. READ also revokes SHARE)
     * @return Permissions that were deleted
     */
	@CompileStatic
	List<Permission> systemRevoke(Userish target, resource, Operation operation=Operation.READ) {
		return performRevoke(false, target, resource, operation)
	}

	/**
	 * As a SecUser, revoke a Permission.
     */
	@CompileStatic
	List<Permission> revoke(SecUser revoker, Permission permission, boolean logIfDenied=true) {
		Object resource = getResourceFromPermission(permission)
		if (!canShare(revoker, resource)) {
			throwAccessControlException(revoker, resource, logIfDenied)
		}
		return systemRevoke(permission)
	}

	/**
	 * Revoke a Permission (as sudo/system)
	 */
	List<Permission> systemRevoke(Permission permission) {
		return performRevoke(
			permission.anonymous,
			permission.user ?: permission.invite ?: permission.key,
			getResourceFromPermission(permission),
			permission.operation)
	}

	/**
	 * Transfer all Permissions created for a SignupInvite to corresponding SecUser (based on email)
	 * @param user to transfer to
     * @return List of Permissions transferred from SignupInvite to SecUser
     */
	List<Permission> transferInvitePermissionsTo(SecUser user) {
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

	private static boolean hasPermission(Userish userish, resource, Operation op) {
		userish = userish?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		def p = Permission.withCriteria {
			eq(resourceProp, resource)
			eq("operation", op)
			or {
				eq("anonymous", true)
				if (isNotNullAndIdNotNull(userish)) {
					String userProp = getUserPropertyName(userish)
					eq(userProp, userish)
				}
			}
		}
		return !p.empty
	}

	/** find Permissions that will be revoked, and cascade according to alsoRevoke map */
	private List<Permission> performRevoke(boolean anonymous, Userish target, resource, Operation operation) {
		target = target?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		List<Permission> permissionList = Permission.withCriteria {
			eq(resourceProp, resource)
			if (anonymous) {
				eq("anonymous", true)
			} else {
				String userProp = getUserPropertyName(target)
				eq(userProp, target)
			}
		}

		log.info("performRevoke: Found permissions for $resource: $permissionList")

		List<Permission> revoked = []
		def revokeOp = { Operation op ->
			permissionList.findAll { it.operation == op }.each { Permission perm ->
				revoked.add(perm)
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
		ALSO_REVOKE.get(operation.id).each(revokeOp)
		return revoked
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

	private static List<Permission> generateDummyOwnerPermissions(resource) {
		String resourceProp = getResourcePropertyName(resource)
		if (hasOwner(resource)) {
			return Operation.enumConstants.collect {
				new Permission(
					id: null,
					user: resource.user,
					operation: it,
					(resourceProp): resource
				)
			}
		} else {
			return []
		}
	}

	@CompileStatic
	private static Object getResourceFromPermission(Permission p) {
		return p.canvas ?: p.dashboard ?: p.feed ?: p.modulePackage ?: p.stream
	}

	@CompileStatic
	private static String getResourcePropertyNameFromClass(Class<?> clazz) {
		if (clazz == Canvas.class) {
			return "canvas"
		} else if (clazz == Dashboard.class) {
			return "dashboard"
		} else if (clazz == Feed.class) {
			return "feed"
		} else if (clazz == ModulePackage) {
			return "modulePackage"
		} else if (clazz == Stream) {
			return "stream"
		} else {
			throw new IllegalArgumentException("Unexpected resource class: " + clazz)
		}
	}

	@CompileStatic
	private static String getResourcePropertyName(Object resource) {
		if (resource instanceof Canvas) {
			return "canvas"
		} else if (resource instanceof Dashboard) {
			return "dashboard"
		} else if (resource instanceof Feed) {
			return "feed"
		} else if (resource instanceof ModulePackage) {
			return "modulePackage"
		} else if (resource instanceof Stream) {
			return "stream"
		} else {
			throw new IllegalArgumentException("Unexpected resource class: " + resource)
		}
	}

	/**
	 * Return property name for Userish
	 */
	@CompileStatic
	private static String getUserPropertyName(Userish userish) {
		if (userish instanceof SecUser) {
			return "user"
		} else if (userish instanceof SignupInvite) {
			return "invite"
		} else if (userish instanceof Key) {
			return "key"
		} else {
			throw new IllegalArgumentException("Unexpected Userish instance: " + userish)
		}
	}

	/** null is often a valid value (but not a valid user), and means "anonymous Permissions only" */
	private static boolean isNotNullAndIdNotNull(userish) {
		return userish != null && userish.id != null
	}

	/** ownership (if applicable) is stored in each Resource as "user" attribute */
	private static boolean isOwner(Userish userish, resource) {
		SecUser secUser = userish?.resolveToSecUser()
		return secUser && hasOwner(resource) && resource.user.id == secUser.id
	}

	private static boolean hasOwner(resource) {
		return resource?.hasProperty("user") && resource?.user?.id != null
	}
}
