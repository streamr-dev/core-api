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
class PermissionService {
	GrailsApplication grailsApplication

	/**
	 * Check whether user is allowed to perform specified operation on a resource
	 */
	boolean check(Userish userish, Object resource, Operation op) {
		return resource?.id != null && hasPermission(userish, resource, op)
	}

	/**
	 * Throws an exception if user is not allowed to perform specified operation on a resource.
	 */
	void verify(Userish userish, Object resource, Operation op) throws NotPermittedException {
		if (!check(userish, resource, op)) {
			SecUser user = userish?.resolveToUserish()
			throw new NotPermittedException(user?.username, resource.class.simpleName, resource.id, op.id)
		}
	}

	/**
	 * List all Permissions granted on a resource
	 */
	List<Permission> getPermissionsTo(Object resource) {
		String resourceProp = getResourcePropertyName(resource)
		return Permission.findAllWhere([(resourceProp): resource])
	}

	/**
	 * List all Permissions with some Operation right granted on a resource
	 */
	List<Permission> getPermissionsTo(Object resource, Operation op) {
		String resourceProp = getResourcePropertyName(resource)
		return Permission.findAllWhere([(resourceProp): resource, "operation": op])
	}

	/**
	 * List all Permissions that have not expired yet with some Operation right granted on a resource
	 */
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
	List<Permission> getPermissionsTo(Object resource, Userish userish) {
		userish = userish?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		// Direct permissions from database
		List<Permission> directPermissions = Permission.withCriteria {
			eq(resourceProp, resource)
			or {
				eq("anonymous", true)
				if (isNotNullAndIdNotNull(userish)) {
					String userProp = getUserPropertyName(userish)
					eq(userProp, userish)
				}
			}
			or {
				isNull("endsAt")
				gt("endsAt", new Date())
			}
		}.toList()

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
	<T> List<T> get(Class<T> resourceClass, Userish userish, Operation op, Closure resourceFilter = {}) {
		return get(resourceClass, userish, op, false, resourceFilter)
	}

	/** Convenience overload: get all including public, adding a flag for public resources may look cryptic */
	@CompileStatic
	<T> List<T> getAll(Class<T> resourceClass, Userish userish, Operation op, Closure resourceFilter = {}) {
		return get(resourceClass, userish, op, true, resourceFilter)
	}

	/**
	 * Get all resources of given type that the user has specified permission for
	 */
	def <T> List<T> get(Class<T> resourceClass, Userish userish, Operation op, boolean includeAnonymous,
						Closure resourceFilter = {}) {
		userish = userish?.resolveToUserish()

		if (!includeAnonymous && !userish?.id) {
			return []
		}

		Closure permissionCriteria = createUserPermissionCriteria(resourceClass, userish, op, includeAnonymous)

		return resourceClass.withCriteria {
			permissionCriteria.delegate = delegate
			resourceFilter.delegate = delegate
			permissionCriteria()
			resourceFilter()
		}
	}

	/**
	 * Creates a criteria that can be included in the <code>BuildableCriteria</code> of a domain object
	 * (Dashboard, Canvas, Stream etc.) to filter query results so that user has specified permission on
	 * them.
	 */
	Closure createUserPermissionCriteria(Class resourceClass, Userish userish, Operation op, boolean includeAnonymous) {
		userish = userish?.resolveToUserish()

		boolean isUser = isNotNullAndIdNotNull(userish)
		String userProp = isUser ? getUserPropertyName(userish) : null
		String idProperty = getResourcePropertyName(resourceClass)

		return {
			permissions {
				eq("operation", op)
				or {
					if (includeAnonymous) {
						eq("anonymous", true)
					}
					if (isUser) {
						eq(userProp, userish)
					}
				}
				or {
					isNull("endsAt")
					gt("endsAt", new Date())
				}
				projections {
					groupProperty(idProperty)
				}
			}
		}
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
		// TODO CORE-498: check grantor himself has the right he's granting? (e.g. "write")
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

		// When a user is granted read access (subscriber) or write access (publisher) to a stream,
		// we need to set the corresponding inbox stream permissions (see methods comments below).
		if (userProp == "user" && resourceProp == "stream" && (endsAt == null || endsAt.after(new Date()))) {
			checkAndGrantInboxPermissions((SecUser) target, (Stream) resource, operation,
				subscription, endsAt, parentPermission)
		}

		return parentPermission
	}

	private void checkAndGrantInboxPermissions(SecUser user, Stream stream, Operation operation,
									   Subscription subscription, Date endsAt, Permission parentPermission) {
		if (!stream.inbox && !stream.uiChannel) {
			if (operation == Operation.STREAM_SUBSCRIBE) {
				grantNewSubscriberInboxStreamPermissions(user, stream, subscription, endsAt, parentPermission)
			} else if (operation == Operation.STREAM_PUBLISH) {
				grantNewPublisherInboxStreamPermissions(user, stream, subscription, endsAt, parentPermission)
			}
		}
	}

	/**
	 *
	 * Grant the subscriber write permission to the inbox streams of every publisher of the stream.
	 * Also grant every publisher of the stream write permission to the subscriber's inbox streams.
	 *
	 */
	private void grantNewSubscriberInboxStreamPermissions(SecUser subscriber, Stream stream,
														  Subscription subscription, Date endsAt, Permission parent) {
		grantInboxStreamPermissions(subscriber, stream, Operation.STREAM_PUBLISH, subscription, endsAt, parent)
	}

	/**
	 *
	 * Grant the publisher write permission to the inbox streams of every subscriber of the stream.
	 * Also grant every subscriber of the stream write permission to the publisher's inbox streams.
	 *
	 */
	private void grantNewPublisherInboxStreamPermissions(SecUser publisher, Stream stream,
														 Subscription subscription, Date endsAt, Permission parent) {
		grantInboxStreamPermissions(publisher, stream, Operation.STREAM_SUBSCRIBE, subscription, endsAt, parent)
	}

	private void grantInboxStreamPermissions(SecUser user, Stream stream, Operation operation,
											 Subscription subscription, Date endsAt, Permission parent) {
		List<SecUser> otherUsers = getNonExpiredPermissionsTo(stream, operation)*.user
		otherUsers.removeIf { it == null || it.username == user.username }
		// Need to initialize the service below this way because of circular dependencies issues
		// Once we use Grails 3, this could be replaced with Grails Events
		StreamService streamService = grailsApplication.mainContext.getBean(StreamService)
		List<Stream> userInboxes = streamService.getInboxStreams([user])
		List<Stream> otherUsersInboxes = streamService.getInboxStreams(otherUsers)
		for (Stream inbox: otherUsersInboxes) {
			new Permission(
				stream: inbox,
				user: user,
				operation: Operation.STREAM_PUBLISH,
				subscription: subscription,
				endsAt: endsAt,
				parent: parent
			).save(flush: true, failOnError: true)
		}
		for (SecUser u: otherUsers) {
			for (Stream userInbox: userInboxes) {
				new Permission(
					stream: userInbox,
					user: u,
					operation: Operation.STREAM_PUBLISH,
					subscription: subscription,
					endsAt: endsAt,
					parent: parent
				).save(flush: true, failOnError: true)
			}
		}
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

	public void cleanUpExpiredPermissions() {
		Date now = new Date()
		Permission.deleteAll(Permission.findAllByEndsAtLessThan(now))
	}

	private boolean hasPermission(Userish userish, Object resource, Operation op) {
		userish = userish?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		List<Permission> directPermissions = Permission.withCriteria {
			eq(resourceProp, resource)
			eq("operation", op)
			or {
				eq("anonymous", true)
				if (isNotNullAndIdNotNull(userish)) {
					String userProp = getUserPropertyName(userish)
					eq(userProp, userish)
				}
			}
			or {
				isNull("endsAt")
				gt("endsAt", new Date())
			}
		}

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

		List<Permission> permissionList = Permission.withCriteria {
			eq(resourceProp, resource)
			if (anonymous) {
				eq("anonymous", true)
			} else {
				String userProp = getUserPropertyName(target)
				eq(userProp, target)
			}
		}.toList()

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

	private static boolean hasOneOrLessSharePermissionsLeft(Object resource) {
		String resourceProp = getResourcePropertyName(resource)
		Permission.Operation shareOp = Operation.shareOperation(resource)
		Integer n = Permission.createCriteria().count {
			eq(resourceProp, resource)
			eq("operation", shareOp)
		}
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

	/** null is often a valid value (but not a valid user), and means "anonymous Permissions only" */
	private static boolean isNotNullAndIdNotNull(userish) {
		return userish != null && userish.id != null
	}
}
