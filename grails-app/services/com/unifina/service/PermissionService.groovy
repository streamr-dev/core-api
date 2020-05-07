package com.unifina.service

import com.unifina.api.NotPermittedException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.Subscription
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.Userish
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

import java.security.AccessControlException
/**
 * Check, get, grant, and revoke permissions. Maintains Access Control Lists (ACLs) to resources.
 *
 * Complexities handled by PermissionService:
 * 		- anonymous Permissions: checked, and listed for resource, but permitted resources not listed for user
 * 		- Permission owners and grant/revoke targets can be SecUsers or SignupInvites
 * 			=> getUserPropertyName
 */
@Transactional(readOnly = false)
class PermissionService {
	PermissionStore store = new PermissionStore()
	GrailsApplication grailsApplication

	/**
	 * Check whether user is allowed to perform specified operation on a resource
	 */
	@Transactional(readOnly = true)
	boolean check(Userish userish, Object resource, Operation op) {
		return resource?.id != null && hasPermission(userish, resource, op)
	}

	/**
	 * Throws an exception if user is not allowed to perform specified operation on a resource.
	 */
	@Transactional(readOnly = true)
	void verify(Userish userish, Object resource, Operation op) throws NotPermittedException {
		if (!check(userish, resource, op)) {
			SecUser user = userish?.resolveToUserish()
			throw new NotPermittedException(user?.username, resource.class.simpleName, resource.id, op.id)
		}
	}

	/**
	 * List all Permissions granted on a resource
	 */
	@Transactional(readOnly = true)
	List<Permission> getPermissionsTo(Object resource) {
		return getPermissionsTo(resource, true, null)
	}

	/**
	 * List all Permissions granted on a resource.
	 *
	 * @param resource Stream, Canvas or other Streamr resource.
	 * @param subscriptions {@code true} for all permissions and {@code false} for permissions where subscription is {@code null}.
	 * @param op Operation to limit the query result set.
	 * @return List of Permission objects.
	 */
	@Transactional(readOnly = true)
	List<Permission> getPermissionsTo(Object resource, boolean subscriptions, Operation op) {
		String resourceProp = getResourcePropertyName(resource)
		return store.getPermissionsTo(resourceProp, resource, subscriptions, op)
	}

	/**
	 * List all Permissions with some Operation right granted on a resource
	 */
	@Transactional(readOnly = true)
	List<Permission> getPermissionsTo(Object resource, Operation op) {
		return getPermissionsTo(resource, true, op)
	}

	/**
	 * List all Permissions that have not expired yet with some Operation right granted on a resource
	 */
	@Transactional(readOnly = true)
	List<Permission> getNonExpiredPermissionsTo(Object resource, Operation op) {
		// TODO: find a way to do this in a single query instead of filtering results
		List<Permission> results = []
		Date now = new Date()
		for (Permission p: getPermissionsTo(resource, op)) {
			if (p.endsAt == null || p.endsAt.after(now)) {
				results.add(p)
			}
		}
		return results
	}

	/**
	 * List all Permissions granted on a resource to a Userish
	 */
	@Transactional(readOnly = true)
	List<Permission> getPermissionsTo(Object resource, Userish userish) {
		userish = userish?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		// Direct permissions from database
		List<Permission> directPermissions = store.findDirectPermissionsForGetPermissionsTo(resourceProp, resource, userish)

		// Special case of UI channels: they inherit permissions from the associated canvas
		if (resource instanceof Stream && resource.isUIChannel()) {
			Set<Permission> syntheticPermissions = new HashSet<>()
			Key key = null
			SecUser user = null
			if (userish instanceof Key) {
				key = userish
			} else if (userish instanceof SecUser) {
				user = userish
			}
			if (userish != null && isPermissionToStreamViaDashboard(userish, resource)) {
				syntheticPermissions.add(new Permission(
					stream: resource,
					operation: Permission.Operation.STREAM_GET,
					key: key,
					user: user,
				))
				syntheticPermissions.add(new Permission(
					stream: resource,
					operation: Permission.Operation.STREAM_SUBSCRIBE,
					key: key,
					user: user,
				))
			}
			// Streams inherit transitive permissions from canvas
			List<Permission> permissions = getPermissionsTo(resource.uiChannelCanvas, userish)
			for (Permission p : permissions) {
				Set<Operation> operations = CANVAS_TO_STREAM[p.operation]
				if (operations != null) {
					for (Operation op : operations) {
						Permission sp = new Permission(
							stream: resource,
							operation: op,
							key: key,
							user: user,
						)
						syntheticPermissions.add(sp)
					}
				}
			}
			directPermissions.addAll(syntheticPermissions)
		}
		return directPermissions
	}

	// Maps canvas operations to stream operations
	private final static Map<Operation, Set<Operation>> CANVAS_TO_STREAM = [
		(Operation.CANVAS_GET): [Operation.STREAM_GET, Operation.STREAM_SUBSCRIBE],
		(Operation.CANVAS_EDIT): [Operation.STREAM_DELETE],
		(Operation.CANVAS_DELETE): [Operation.STREAM_DELETE],
		(Operation.CANVAS_STARTSTOP): [Operation.STREAM_PUBLISH],
	]

	private boolean isPermissionToStreamViaDashboard(Userish userish, Stream stream) {
		if (userish != null && stream.isUIChannel()) {
			Canvas canvas = stream.uiChannelCanvas
			int moduleId
			try {
				moduleId = stream.parseModuleID()
			} catch (IllegalArgumentException e) {
				return false
			}
			List<DashboardItem> matchedItems = DashboardItem.findAllByCanvasAndModule(canvas, moduleId)
			for (DashboardItem item : matchedItems) {
				if (check(userish, item.dashboard, Operation.DASHBOARD_GET)) {
					return true
				}
			}
		}
		return false
	}

	/** Overload to allow leaving out the anonymous-include-flag but including the filter */
	@CompileStatic
	@Transactional(readOnly = true)
	<T> List<T> get(Class<T> resourceClass, Userish userish, Operation op, Closure resourceFilter = {}) {
		return get(resourceClass, userish, op, false, resourceFilter)
	}

	/** Convenience overload: get all including public, adding a flag for public resources may look cryptic */
	@CompileStatic
	@Transactional(readOnly = true)
	<T> List<T> getAll(Class<T> resourceClass, Userish userish, Operation op, Closure resourceFilter = {}) {
		return get(resourceClass, userish, op, true, resourceFilter)
	}

	/**
	 * Get all resources of given type that the user has specified permission for
	 */
	@Transactional(readOnly = true)
	<T> List<T> get(Class<T> resourceClass, Userish userish, Operation op, boolean includeAnonymous,
						Closure resourceFilter = {}) {
		return store.get(resourceClass, userish, op, includeAnonymous, resourceFilter)
	}

	/**
	 * As a SecUser, attempt to grant Permission to another Userish on resource
	 *
	 * @param grantor user attempting to grant Permission (needs SHARE permission)
	 * @param resource to be given permission on
	 * @param target Userish to be given permission to
	 *
	 * @return Permission if successfully granted
	 *
	 * @throws AccessControlException if grantor doesn't have SHARE permission on resource
	 * @throws IllegalArgumentException if given invalid resource or target
     */
	@CompileStatic
	Permission grant(SecUser grantor,
					 Object resource,
					 Userish target,
					 Operation operation,
					 boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		Operation shareOp = Permission.Operation.shareOperation(resource)
		if (!check(grantor, resource, shareOp)) {
			throwAccessControlException(grantor, resource, logIfDenied)
		}
		return systemGrant(target, resource, operation)
	}

	/**
	 * Grants all permissions to a Userish on given resource (as sudo/system)
	 *
	 * @param target Userish that will receive the access
	 * @param resource to be given permission on
	 *
	 * @return granted permissions (size == 3)
	 */
	@CompileStatic
	List<Permission> systemGrantAll(Userish target, Object resource) {
		Operation.operationsFor(resource).collect { Operation op ->
			systemGrant(target, resource, op)
		}
	}

	/**
	 * Grant Permission to a Userish (as sudo/system)
	 *
	 * @param target Userish that will receive the access
	 * @param resource to be given permission on
	 *
     * @return granted permission
     */
	Permission systemGrant(Userish target, Object resource, Operation operation) {
		return systemGrant(target, resource, operation, null, null)
	}

	Permission systemGrant(Userish target, Object resource, Operation operation, Subscription subscription, Date endsAt) {
		if (target == null) {
			throw new IllegalArgumentException("Permission grant target can't be null")
		}
		if (operation == null) {
			throw new IllegalArgumentException("Operation can't be null")
		}
		target = target.resolveToUserish()
		String userProp = getUserPropertyName(target)
		String resourceProp = getResourcePropertyName(resource)

		Permission parentPermission = new Permission(
			(resourceProp): resource,
			(userProp): target,
			operation: operation,
			subscription: subscription,
			endsAt: endsAt
		).save(flush: true, failOnError: true)

		return parentPermission
	}

	/**
	 *
	 * Grant anonymous (public) Permission on a resource so that anyone can access it
	 *
	 * @param grantor user attempting to grant Permission (needs SHARE permission)
	 * @param resource resource to be given public access to
	 *
	 * @return Permission if successfully granted
	 *
	 * @throws AccessControlException if grantor doesn't have SHARE permission on resource
	 * @throws IllegalArgumentException if given invalid resource
	 */
	@CompileStatic
	Permission grantAnonymousAccess(SecUser grantor,
									Object resource,
									Operation operation,
									boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		Operation shareOp = Permission.Operation.shareOperation(resource)
		if (!check(grantor, resource, shareOp)) {
			throwAccessControlException(grantor, resource, logIfDenied)
		}
		return systemGrantAnonymousAccess(resource, operation)
	}

	/**
	 * Grant anonymous (public) Permission on a resource (as sudo/system) so that anyone can access it
	 *
	 * @param resource to be given permission on
	 *
	 * @return granted permission
	 */
	Permission systemGrantAnonymousAccess(Object resource, Operation operation) {
		String resourceProp = getResourcePropertyName(resource)
		return new Permission(
			(resourceProp): resource,
			operation: operation,
			anonymous: true
		).save(flush: true, failOnError: true)
	}

	/**
	 * As a SecUser, revoke a Permission from a Userish
	 *
	 * @param revoker user attempting to revoke permission (needs *_share permission)
	 * @param resource to be revoked from target
	 * @param target Userish user whose Permission is revoked
	 * @param operation or access level to be revoked
	 *
	 * @returns Permissions that were deleted
	 *
	 * @throws AccessControlException if revoker doesn't have *_share permission on resource
     */
	@CompileStatic
	List<Permission> revoke(SecUser revoker,
							Object resource,
							Userish target,
							Operation operation,
							boolean logIfDenied = true) throws AccessControlException {
		if (operation == null) {
			throw new IllegalArgumentException("Operation can't be null")
		}
		Operation shareOp = Permission.Operation.shareOperation(resource)
		if (!check(revoker, resource, shareOp)) {
			throwAccessControlException(revoker, resource, logIfDenied)
		}
		return systemRevoke(target, resource, operation)
	}

	/**
	 * Revoke a Permission from a Userish (as sudo/system)
	 *
	 * @param target Userish whose Permission is revoked
	 * @param resource to be revoked from target
	 * @param operation or access level to be revoked
	 *
     * @return Permissions that were deleted
     */
	@CompileStatic
	List<Permission> systemRevoke(Userish target, Object resource, Operation operation) {
		if (operation == null) {
			throw new IllegalArgumentException("Operation can't be null")
		}
		boolean anonymous = false
		return performRevoke(anonymous, target, resource, operation)
	}

	/**
	 * Revoke anonymous (public) Permission to a resource (as sudo/system)
	 *
	 * @param resource to be revoked anonymous/public access to
	 *
	 * @return Permissions that were deleted
	 */
	@CompileStatic
	List<Permission> systemRevokeAnonymousAccess(Object resource, Operation operation) {
		if (operation == null) {
			throw new IllegalArgumentException("Operation can't be null")
		}
		boolean anonymous = true
		Userish target = null
		return performRevoke(anonymous, target, resource, operation)
	}

	/**
	 * As a SecUser, revoke a Permission.
	 *
	 * @param revoker user attempting to revoke permission (needs SHARE permission)
	 * @param permission to be revoked
	 *
	 * @return Permissions that were deleted
	 *
	 * @throws AccessControlException if revoker doesn't have SHARE permission on resource
	 */
	@CompileStatic
	List<Permission> revoke(SecUser revoker, Permission permission, boolean logIfDenied=true)
			throws AccessControlException {
		Object resource = getResourceFromPermission(permission)
		Permission.Operation shareOp = Operation.shareOperation(resource)
		if (!check(revoker, resource, shareOp)) {
			throwAccessControlException(revoker, resource, logIfDenied)
		}
		return systemRevoke(permission)
	}

	/**
	 * Revoke a Permission (as sudo/system)
	 *
	 * @return Permissions that were deleted
	 */
	@CompileStatic
	List<Permission> systemRevoke(Permission permission) {
		return performRevoke(
			permission.anonymous,
			permission.user ?: permission.invite ?: permission.key,
			getResourceFromPermission(permission),
			permission.operation)
	}

	/**
	 * Transfer all Permissions created for a SignupInvite to the corresponding SecUser (based on email)
	 *
	 * @param user to transfer to
     * @return List of Permissions transferred from SignupInvite to SecUser
     */
	List<Permission> transferInvitePermissionsTo(SecUser user) {
		// { invite { eq "username", user.username } } won't do: some invite are null => NullPointerException
		return store.findPermissionsToTransfer().findAll {
			it.invite.username == user.username
		}.collect { p ->
			p.invite = null
			p.user = user
			p.save(flush: true, failOnError: true)
		}
	}

	public void cleanUpExpiredPermissions() {
		Date now = new Date()
		Permission.deleteAll(Permission.findAllByEndsAtLessThan(now))
	}

	private boolean hasPermission(Userish userish, Object resource, Operation op) {
		userish = userish?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		List<Permission> directPermissions = store.findDirectPermissionsForHasPermission(resourceProp, resource, op, userish)

		// Special case of UI channels: they inherit permissions from the associated canvas
		if (directPermissions.isEmpty() && resource instanceof Stream && resource.uiChannel) {
			for (Permission perm : getPermissionsTo(resource, userish)) {
				if (perm.operation == op) {
					return true
				}
			}
		}

		return !directPermissions.isEmpty()
	}

	/**
	 * Find Permissions that will be revoked
	 */
	private List<Permission> performRevoke(boolean anonymous, Userish target, Object resource, Operation operation) {
		target = target?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		List<Permission> permissionList = store.findPermissionsToRevoke(resourceProp, resource, anonymous, target)

		// Prevent revocation of only/last share permission to prevent inaccessible resources
		if (hasOneOrLessSharePermissionsLeft(resource) && Operation.shareOperation(resource) in permissionList*.operation) {
			throw new AccessControlException("Cannot revoke only SHARE permission of ${resource}")
		}

		log.info("performRevoke: Found permissions for $resource: $permissionList")

		List<Permission> revoked = []
		def revokeOp = { Operation op ->
			permissionList.findAll { Permission perm ->
				perm.operation == op
			}.each { Permission perm ->
				revoked.add(perm)
				List<Permission> childPermissions = Permission.findAllByParent(perm)
				revoked.addAll(childPermissions)
				try {
					log.info("performRevoke: Trying to delete permission $perm.id")
					Permission.withNewTransaction {
						perm.delete(flush: true)
						for (Permission childPerm: childPermissions) {
							childPerm.delete(flush: true)
						}
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
		return revoked
	}

	private boolean hasOneOrLessSharePermissionsLeft(Object resource) {
		String resourceProp = getResourcePropertyName(resource)
		int n = store.countSharePermissions(resourceProp, resource)
		return n <= 1
	}

	private void throwAccessControlException(SecUser violator, Object resource, boolean loggingEnabled) {
		if (loggingEnabled) {
			log.warn("${violator?.username}(id ${violator?.id}) tried to modify sharing of $resource without SHARE Permission!")
		}
		throw new AccessControlException("${violator?.username}(id ${violator?.id}) has no 'share' permission to $resource!")
	}

	private static Object getResourceFromPermission(Permission p) {
		String field = Permission.resourceFields.find { p[it] }
		if (field == null) {
			throw new IllegalArgumentException("No known resource attached to " + p)
		}
		return p[field]
	}

	@CompileStatic
	private static String getResourcePropertyName(Object resource) {
		// Cannot derive name straight from resource.getClass() because of proxy assist objects!
		Class resourceClass = resource instanceof Class ? resource : resource.getClass()
		if (Canvas.isAssignableFrom(resourceClass)) {
			return "canvas"
		} else if (Dashboard.isAssignableFrom(resourceClass)) {
			return "dashboard"
		} else if (Product.isAssignableFrom(resourceClass)) {
			return "product"
		} else if (Stream.isAssignableFrom(resourceClass)) {
			return "stream"
		} else {
			throw new IllegalArgumentException("Unexpected resource class: " + resourceClass)
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
}
