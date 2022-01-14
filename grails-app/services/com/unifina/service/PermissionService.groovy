package com.unifina.service
import com.streamr.network.contract.StreamRegistry
import com.unifina.domain.*
import com.unifina.domain.Permission.Operation
import com.unifina.utils.ApplicationConfig
import grails.compiler.GrailsCompileStatic
import grails.transaction.Transactional
import grails.util.Holders
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteFunctionCall

import java.security.AccessControlException

/**
 * Check, get, grant, and revoke permissions. Maintains Access Control Lists (ACLs) to resources.
 *
 * Complexities handled by PermissionService:
 * 		- anonymous Permissions: checked, and listed for resource, but permitted resources not listed for user
 * 		- Permission owners and grant/revoke targets can be Users => getUserPropertyName
 */
@GrailsCompileStatic
class PermissionService {
	PermissionStore store = new PermissionStore()

	private String findID(Object resource) {
		if (resource == null) {
			return null
		}
		if (resource instanceof Product) {
			return ((Product) resource).getId()
		}
		return null
	}

	/**
	 * Check whether user is allowed to perform specified operation on a resource
	 */
	boolean check(User user, Product resource, Operation op) {
		String id = findID(resource)
		return id != null && hasPermission(user, resource, op)
	}

	boolean checkAnonymousAccess(Product resource, Operation op) {
		return check(null, resource, op)
	}

	/**
	 * Throws an exception if user is not allowed to perform specified operation on a streamId.
	 */
	void verify(Web3j web3j, String userAddress, String streamId, StreamPermission permission) throws NotPermittedException {
		log.debug(String.format("Checking users %s Stream %s permission to Stream %s", userAddress, permission, streamId))
		String streamRegistryAddress = ApplicationConfig.getString("streamr.ethereum.streamRegistryAddress")
		Credentials credentials = Credentials.create(ApplicationConfig.getString("streamr.ethereum.nodePrivateKey"))
		StreamrGasProvider gasProvider = StreamrGasProvider.createStreamrGasProvider()
		StreamRegistry streamRegistry = StreamRegistry.load(streamRegistryAddress, web3j, credentials, gasProvider)
		RemoteFunctionCall<Boolean> hasPermissionCall = streamRegistry.hasPermission(streamId, userAddress, permission.toBigInteger())
		Boolean hasPermission
		try {
			hasPermission = hasPermissionCall.send()
		} catch (Exception e) {
			String msg = String.format("Function call to Ethereum failed with error: %s", e)
			throw new BlockchainException(msg)
		}
		log.debug(String.format("Users %s permission %s to stream %s is %s", userAddress, permission, streamId, hasPermission))
		if (!hasPermission) {
			throw new NotPermittedException(userAddress, "Stream", streamId, "Grant")
		}
	}

	/**
	 * Throws an exception if user is not allowed to perform specified operation on a product.
	 */
	void verify(User user, Product product, Operation op) throws NotPermittedException {
		if (!check(user, product, op)) {
			String name = ""
			if (user != null) {
				name = user.username
			}
			throw new NotPermittedException(name, Product.simpleName, findID(product), op.id)
		}
	}

	/**
	 * List all Permissions granted on a resource
	 */
	@Transactional(readOnly = true)
	List<Permission> getPermissionsTo(Product resource) {
		return getPermissionsTo(resource, true, null)
	}

	/**
	 * List all Permissions granted on a resource.
	 *
	 * @param resource Product
	 * @param subscriptions {@code true} for all permissions and {@code false} for permissions where subscription is {@code null}.
	 * @param op Operation to limit the query result set.
	 * @return List of Permission objects.
	 */
	@Transactional(readOnly = true)
	List<Permission> getPermissionsTo(Product resource, boolean subscriptions, Operation op) {
		return store.getPermissionsTo(resource, subscriptions, op)
	}

	/**
	 * List all Permissions with some Operation right granted on a resource
	 */
	@Transactional(readOnly = true)
	List<Permission> getPermissionsTo(Product resource, Operation op) {
		return getPermissionsTo(resource, true, op)
	}

	/**
	 * List all Permissions granted on a resource to a User
	 */
	@Transactional(readOnly = true)
	List<Permission> getPermissionsTo(Product resource, User user) {
		// Direct permissions from database
		List<Permission> directPermissions = store.findDirectPermissions(resource, null as Operation, user)
		return directPermissions
	}

	// TODO: What about anonymous permissions?

	/** Overload to allow leaving out the anonymous-include-flag but including the filter */
	@Transactional(readOnly = true)
	public List<Product> get(User user, Operation op, Closure resourceFilter = {}) {
		return get(user, op, false, resourceFilter)
	}

	/** Convenience overload: get all including public, adding a flag for public resources may look cryptic */
	@Transactional(readOnly = true)
	public List<Product> getAll(User user, Operation op, Closure resourceFilter = {}) {
		return get(user, op, true, resourceFilter)
	}

	/**
	 * Get all resources of given type that the user has specified permission for
	 */
	@Transactional(readOnly = true)
	public List<Product> get(User user, Operation op, boolean includeAnonymous, Closure resourceFilter = {}) {
		return store.get(user, op, includeAnonymous, resourceFilter)
	}

	/**
	 * As a User, attempt to grant Permission to an User on resource
	 *
	 * @param grantor user attempting to grant Permission (needs SHARE permission)
	 * @param resource to be given permission on
	 * @param target User to be given permission to
	 *
	 * @return Permission if successfully granted
	 *
	 * @throws AccessControlException if grantor doesn't have SHARE permission on resource
	 * @throws IllegalArgumentException if given invalid resource or target
	 */
	Permission grant(User grantor,
		Product resource,
		User target,
		Operation operation,
		boolean logIfDenied = true) throws AccessControlException, IllegalArgumentException {
		if (!check(grantor, resource, Permission.Operation.PRODUCT_SHARE)) {
			throwAccessControlException(grantor, resource, logIfDenied)
		}
		return systemGrant(target, resource, operation)
	}

	/**
	 * Grants all permissions to a User on given resource (as sudo/system)
	 *
	 * @param target User that will receive the access
	 * @param resource to be given permission on
	 *
	 * @return granted permissions (size == 3)
	 */
	List<Permission> systemGrantAll(User target, Product resource) {
		Operation.productOperations().collect { Operation op ->
			systemGrant(target, resource, op)
		}
	}

	/**
	 * Grant Permission to a User (as sudo/system)
	 *
	 * @param target User that will receive the access
	 * @param resource to be given permission on
	 *
	 * @return granted permission
	 */
	Permission systemGrant(User target, Product resource, Operation operation) {
		return systemGrant(target, resource, operation, null, null)
	}

	Permission systemGrant(User target, Product resource, Operation operation, Subscription subscription, Date endsAt) {
		if (target == null) {
			throw new IllegalArgumentException("Permission grant target can't be null")
		}
		if (operation == null) {
			throw new IllegalArgumentException("Operation can't be null")
		}

		Permission parentPermission = new Permission(
			product: resource,
			user: target,
			operation: operation,
			subscription: subscription,
			endsAt: endsAt
		).save(flush: false, failOnError: true)

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
	Permission grantAnonymousAccess(User grantor,
		Product resource,
		Operation operation,
		boolean logIfDenied = true) throws AccessControlException, IllegalArgumentException {
		if (!check(grantor, resource, Permission.Operation.PRODUCT_SHARE)) {
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
	Permission systemGrantAnonymousAccess(Product resource, Operation operation) {
		return new Permission(
			product: resource,
			operation: operation,
			anonymous: true
		).save(flush: false, failOnError: true)
	}

	/**
	 * As a User, revoke a Permission from a User
	 *
	 * @param revoker user attempting to revoke permission (needs *_share permission)
	 * @param resource to be revoked from target
	 * @param target User user whose Permission is revoked
	 * @param operation or access level to be revoked
	 *
	 * @returns Permissions that were deleted
	 *
	 * @throws AccessControlException if revoker doesn't have *_share permission on resource
	 */
	List<Permission> revoke(User revoker,
		Product resource,
		User target,
		Operation operation,
		boolean logIfDenied = true) throws AccessControlException {
		if (operation == null) {
			throw new IllegalArgumentException("Operation can't be null")
		}
		if (!check(revoker, resource, Permission.Operation.PRODUCT_SHARE)) {
			throwAccessControlException(revoker, resource, logIfDenied)
		}
		return systemRevoke(target, resource, operation)
	}

	/**
	 * Revoke a Permission from a User (as sudo/system)
	 *
	 * @param target User whose Permission is revoked
	 * @param resource to be revoked from target
	 * @param operation or access level to be revoked
	 *
	 * @return Permissions that were deleted
	 */
	List<Permission> systemRevoke(User target, Product resource, Operation operation) {
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
	List<Permission> systemRevokeAnonymousAccess(Product resource, Operation operation) {
		if (operation == null) {
			throw new IllegalArgumentException("Operation can't be null")
		}
		boolean anonymous = true
		User target = null
		return performRevoke(anonymous, target, resource, operation)
	}

	/**
	 * As a User, revoke a Permission.
	 *
	 * @param revoker user attempting to revoke permission (needs SHARE permission)
	 * @param permission to be revoked
	 *
	 * @return Permissions that were deleted
	 *
	 * @throws AccessControlException if revoker doesn't have SHARE permission on resource
	 */
	List<Permission> revoke(User revoker, Permission permission, boolean logIfDenied = true)
		throws AccessControlException {
		Product resource = permission.product
		if (!check(revoker, resource, Operation.PRODUCT_SHARE)) {
			throwAccessControlException(revoker, resource, logIfDenied)
		}
		return systemRevoke(permission)
	}

	/**
	 * Revoke a Permission (as sudo/system)
	 *
	 * @return Permissions that were deleted
	 */
	List<Permission> systemRevoke(Permission permission) {
		return performRevoke(permission.anonymous, permission.user, permission.product, permission.operation)
	}

	public void cleanUpExpiredPermissions() {
		Date now = new Date()
		Permission.deleteAll(Permission.findAllByEndsAtLessThan(now))
	}

	private boolean hasPermission(User user, Product resource, Operation op) {
		List<Permission> directPermissions = store.findDirectPermissions(resource, op, user)
		return !directPermissions.isEmpty()
	}

	/**
	 * Find Permissions that will be revoked
	 */
	private List<Permission> performRevoke(boolean anonymous, User target, Product resource, Operation operation) {
		List<Permission> permissionList = store.findPermissionsToRevoke(resource, anonymous, target)

		// Prevent revocation of only/last share permission to prevent inaccessible resources
		Operation shareOperation = Operation.PRODUCT_SHARE
		if (operation == shareOperation && hasOneOrLessSharePermissionsLeft(resource) && shareOperation in permissionList*.operation) {
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
						perm.delete(flush: false)
						for (Permission childPerm : childPermissions) {
							childPerm.delete(flush: false)
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

	private boolean hasOneOrLessSharePermissionsLeft(Product resource) {
		int n = store.countSharePermissions(resource)
		return n <= 1
	}

	private void throwAccessControlException(User violator, Object resource, boolean loggingEnabled) {
		if (loggingEnabled) {
			log.warn("${violator?.username}(id ${violator?.id}) tried to modify sharing of $resource without SHARE Permission!")
		}
		throw new AccessControlException("${violator?.username}(id ${violator?.id}) has no 'share' permission to $resource!")
	}

	Permission savePermission(User apiUser, Operation op, String targetUsername, Resource resource) {
		User target = User.findByUsername(targetUsername)
		Permission permission = savePermissionPrivate(apiUser, op, target, resource)
		return permission
	}

	Permission saveAnonymousPermission(User apiUser, Operation op, Resource resource) {
		Product res = resource.load(apiUser, true)
		Permission permission = grantAnonymousAccess(apiUser, res, op)
		return permission
	}

	private Permission savePermissionPrivate(User apiUser, Operation op, User targetUser, Resource resource) {
		Product res = resource.load(apiUser, true)
		Permission permission = grant(apiUser, res, targetUser, op)
		return permission
	}

	Permission savePermissionForEthereumAccount(String username, User grantor, Operation operation, Resource res, SignupMethod signupMethod) {
		EthereumUserService ethereumUserService = Holders.getApplicationContext().getBean(EthereumUserService)
		User user = ethereumUserService.getOrCreateFromEthereumAddress(username, signupMethod)
		User target = User.findByUsername(user.username)
		Permission newPermission = savePermissionPrivate(grantor, operation, target, res)
		return newPermission
	}

	@Transactional(readOnly = true)
	List<Permission> getOwnPermissions(Resource resource, User apiUser) {
		Product res = resource.load(apiUser, false)
		List<Permission> results = getPermissionsTo(res, apiUser)
		return results
	}

	@Transactional(readOnly = true)
	List<Permission> findAllPermissions(Resource resource, User apiUser, boolean subscriptions) {
		Product res = resource.load(apiUser, true)
		List<Permission> permissions = getPermissionsTo(res, subscriptions, null)
		return permissions
	}

	@Transactional(readOnly = true)
	Permission findPermission(Long permissionId, Resource resource, User apiUser) {
		Product res = resource.load(apiUser, true)
		List<Permission> permissions = getPermissionsTo(res)
		Permission p = permissions.find { it.id == permissionId }
		if (!p) {
			throw new NotFoundException("Permission not found", "Product", permissionId?.toString())
		}
		return p
	}

	void deletePermission(Long permissionId, Resource resource, User apiUser) {
		Product res = resource.load(apiUser, false)
		List<Permission> permissions = getPermissionsTo(res)
		Permission p = permissions.find { it.id == permissionId }
		if (!p) {
			throw new NotFoundException("Permission not found", "Product", permissionId?.toString())
		}
		boolean canShare = check(apiUser, res, Permission.Operation.PRODUCT_SHARE)
		if (canShare == false && p.user == apiUser) {
			// user without share permission to resource can delete their own permission to resource
			systemRevoke(p)
		} else if (canShare) {
			// user with share permission to resource can delete another user's permission to same resource
			systemRevoke(p)
		} else {
			// user without share permission to resource can't delete another user's permission to same resource
			throw new NotPermittedException("User without share permission to resource can't delete another user's permission to same resource.")
		}
	}
}
