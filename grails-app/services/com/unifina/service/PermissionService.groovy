package com.unifina.service

import com.unifina.api.NotPermittedException
import com.unifina.domain.dashboard.Dashboard

import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.Subscription
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.security.Userish
import groovy.transform.CompileStatic

import java.security.AccessControlException

/**
 * Check, get, grant, and revoke permissions. Maintains Access Control Lists (ACLs) to resources.
 *
 * Complexities handled by PermissionService:
 * 		- anonymous Permissions: checked, and listed for resource, but permitted resources not listed for user
 * 		- Permission owners and grant/revoke targets can be SecUsers or SignupInvites
 * 			=> getUserPropertyName
 * 		- combinations of read/write/share (RWS) should be restricted, e.g. to disallow write without read?
 * 			=> ALSO_REVOKE
 * 			TODO: discuss... current implementation with ALSO_REVOKE but no ALSO_GRANT is conservative but unsymmetric
 */
class PermissionService {
	// Cascade revocations to "higher" rights to ensure meaningful combinations (e.g. WRITE without READ makes no sense)
	private static final Map<String, List<Operation>> ALSO_REVOKE = [read: [Operation.WRITE, Operation.SHARE]]

	def grailsApplication

	/**
	 * Check whether user is allowed to read a resource
	 */
	@CompileStatic
	boolean canRead(Userish userish, resource)  {
		return check(userish, resource, Operation.READ)
	}

	/**
	 * Throws an exception if user is not allowed to read a resource
	 */
	@CompileStatic
	void verifyRead(Userish userish, resource) throws NotPermittedException {
		verify(userish, resource, Operation.READ)
	}

	/**
	 * Check whether user is allowed to write a resource
	 */

	@CompileStatic
	boolean canWrite(Userish userish, resource) {
		return check(userish, resource, Operation.WRITE)
	}

	/**
	 * Throws an exception if user is not allowed to write a resource
	 */
	@CompileStatic
	void verifyWrite(Userish userish, resource) throws NotPermittedException {
		verify(userish, resource, Operation.WRITE)
	}

	/**
	 * Check whether user is allowed to share a resource
	 */
	@CompileStatic
	boolean canShare(Userish userish, resource) {
		return check(userish, resource, Operation.SHARE)
	}

	/**
	 * Throws an exception if user is not allowed to share a resource
	 */
	@CompileStatic
	void verifyShare(Userish userish, resource) throws NotPermittedException {
		verify(userish, resource, Operation.SHARE)
	}

	/**
	 * Check whether user is allowed to perform specified operation on a resource
	 */
	boolean check(Userish userish, resource, Operation op) {
		return resource?.id != null && hasPermission(userish, resource, op)
	}

	/**
	 * Throws an exception if user is not allowed to perform specified operation on a resource.
	 */
	void verify(Userish userish, resource, Operation op) throws NotPermittedException {
		if (!check(userish, resource, op)) {
			SecUser user = userish?.resolveToUserish()
			throw new NotPermittedException(user?.username, resource.class.simpleName, resource.id, op.id)
		}
	}

	/**
	 * List all Permissions granted on a resource
	 */
	List<Permission> getPermissionsTo(resource) {
		String resourceProp = getResourcePropertyName(resource)
		return Permission.findAllWhere([(resourceProp): resource])
	}

	/**
	 * List all Permissions with some Operation right granted on a resource
	 */
	List<Permission> getPermissionsTo(resource, Operation op) {
		String resourceProp = getResourcePropertyName(resource)
		return Permission.findAllWhere([(resourceProp): resource, "operation": op])
	}

	/**
	 * List all Permissions that have not expired yet with some Operation right granted on a resource
	 */
	List<Permission> getNonExpiredPermissionsTo(resource, Operation op) {
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
	List<Permission> getPermissionsTo(resource, Userish userish) {
		userish = userish?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		List directPermissions = Permission.withCriteria {
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
		if (resource instanceof Stream && resource.uiChannel) {
			directPermissions.addAll(getPermissionsTo(resource.uiChannelCanvas, userish))
		}

		return directPermissions
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
					 resource,
					 Userish target,
					 Operation operation=Operation.READ,
					 boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		// TODO CORE-498: check grantor himself has the right he's granting? (e.g. "write")
		if (!canShare(grantor, resource)) {
			throwAccessControlException(grantor, resource, logIfDenied)
		}
		return systemGrant(target, resource, operation)
	}

	/**
	 * Grants all Permissions (READ, WRITE, SHARE) to a Userish (as sudo/system)
	 *
	 * @param target Userish that will receive the access
	 * @param resource to be given permission on
	 *
	 * @return granted permissions (size == 3)
	 */
	@CompileStatic
	List<Permission> systemGrantAll(Userish target, resource) {
		Operation.values().collect { Operation op ->
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
	Permission systemGrant(Userish target, resource, Operation operation=Operation.READ) {
		return systemGrant(target, resource, operation, null, null)
	}

	Permission systemGrant(Userish target, resource, Operation operation=Operation.READ, Subscription subscription, Date endsAt) {
		if (target == null) {
			throw new IllegalArgumentException("Permission grant target can't be null");
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
			if (operation == Operation.READ) {
				grantNewSubscriberInboxStreamPermissions(user, stream, subscription, endsAt, parentPermission)
			} else if (operation == Operation.WRITE) {
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
		grantInboxStreamPermissions(subscriber, stream, Operation.WRITE, subscription, endsAt, parent)
	}

	/**
	 *
	 * Grant the publisher write permission to the inbox streams of every subscriber of the stream.
	 * Also grant every subscriber of the stream write permission to the publisher's inbox streams.
	 *
	 */
	private void grantNewPublisherInboxStreamPermissions(SecUser publisher, Stream stream,
														 Subscription subscription, Date endsAt, Permission parent) {
		grantInboxStreamPermissions(publisher, stream, Operation.READ, subscription, endsAt, parent)
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
				operation: Operation.WRITE,
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
					operation: Operation.WRITE,
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
									resource,
									Operation operation=Operation.READ,
									boolean logIfDenied=true) throws AccessControlException, IllegalArgumentException {
		if (!canShare(grantor, resource)) {
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
	 *
	 * @param revoker user attempting to revoke permission (needs SHARE permission)
	 * @param resource to be revoked from target
	 * @param target Userish user whose Permission is revoked
	 * @param operation or access level to be revoked (cascades to "higher" operations, e.g. READ also revokes SHARE)
	 *
	 * @returns Permissions that were deleted
	 *
	 * @throws AccessControlException if revoker doesn't have SHARE permission on resource
     */
	@CompileStatic
	List<Permission> revoke(SecUser revoker,
							resource,
							Userish target,
							Operation operation=Operation.READ,
							boolean logIfDenied=true) throws AccessControlException {
		if (!canShare(revoker, resource)) {
			throwAccessControlException(revoker, resource, logIfDenied)
		}
		return systemRevoke(target, resource, operation)
	}

	/**
	 * Revoke a Permission from a Userish (as sudo/system)
	 *
	 * @param target Userish whose Permission is revoked
	 * @param resource to be revoked from target
	 * @param operation or access level to be revoked (cascades to "higher" operations, e.g. READ also revokes SHARE)
	 *
     * @return Permissions that were deleted
     */
	@CompileStatic
	List<Permission> systemRevoke(Userish target, resource, Operation operation=Operation.READ) {
		return performRevoke(false, target, resource, operation)
	}

	/**
	 * Revoke anonymous (public) Permission to a resource (as sudo/system)
	 *
	 * @param resource to be revoked anonymous/public access to
	 *
	 * @return Permissions that were deleted
	 */
	@CompileStatic
	List<Permission> systemRevokeAnonymousAccess(resource, Operation operation=Operation.READ) {
		return performRevoke(true, null, resource, operation)
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
		if (!canShare(revoker, resource)) {
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

	private boolean hasPermission(Userish userish, resource, Operation op) {
		userish = userish?.resolveToUserish()
		String resourceProp = getResourcePropertyName(resource)

		List<Permission> p = Permission.withCriteria {
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
		if (p.empty && resource instanceof Stream && resource.uiChannel) {
			return hasPermission(userish, resource.uiChannelCanvas, op)
		}

		return !p.empty
	}

	/**
	 * Find Permissions that will be revoked, and cascade according to alsoRevoke map
	 */
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
		}.toList()

		// Prevent revocation of only/last share permission to prevent inaccessible resources
		if (hasOneOrLessSharePermissionsLeft(resource) && Operation.SHARE in permissionList*.operation) {
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
		ALSO_REVOKE.get(operation.id).each(revokeOp)
		return revoked
	}

	private static boolean hasOneOrLessSharePermissionsLeft(resource) {
		String resourceProp = getResourcePropertyName(resource)
		def criteria = Permission.createCriteria()
		def n = criteria.count {
			eq(resourceProp, resource)
			eq("operation", Operation.SHARE)
		}
		return n <= 1
	}

	private void throwAccessControlException(SecUser violator, resource, boolean loggingEnabled) {
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
		} else if (ModulePackage.isAssignableFrom(resourceClass)) {
			return "modulePackage"
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
