package com.unifina.service

import com.unifina.domain.security.Permission
import com.unifina.security.Userish

class PermissionStore {
	/** null is often a valid value (but not a valid user), and means "anonymous Permissions only" */
	private static boolean isNotNullAndIdNotNull(Userish userish) {
		return userish != null && userish.id != null
	}

	List<Permission> findDirectPermissions(String resourceProp, Object resource, Permission.Operation operation, Userish userish) {
		List<Permission> directPermissions = Permission.withCriteria {
			eq(resourceProp, resource)
			if (operation != null) {
				eq("operation", operation)
			}
			or {
				eq("anonymous", true)
				if (isNotNullAndIdNotNull(userish)) {
					String userProp = PermissionService.getUserPropertyName(userish)
					eq(userProp, userish)
				}
			}
			or {
				isNull("endsAt")
				gt("endsAt", new Date())
			}
		}
		return directPermissions
	}

	List<Permission> getPermissionsTo(String resourceProp, resource, boolean subscriptions, Permission.Operation op) {
		List<Permission> results = Permission.createCriteria().list() {
			eq(resourceProp, resource)
			if (!subscriptions) {
				eq('subscription', null)
			}
			if (op) {
				eq('operation', op)
			}
		}
		return results
	}

	int countSharePermissions(String resourceProp, resource) {
		Permission.Operation shareOperation = Permission.Operation.shareOperation(resource)
		Integer n = Permission.createCriteria().count {
			eq(resourceProp, resource)
			eq("operation", shareOperation)
		}
		return n
	}

	List<Permission> findPermissionsToRevoke(String resourceProp, resource, boolean anonymous, Userish target) {
		List<Permission> permissionList = Permission.withCriteria {
			eq(resourceProp, resource)
			if (anonymous) {
				eq("anonymous", true)
			} else {
				String userProp = PermissionService.getUserPropertyName(target)
				eq(userProp, target)
			}
		}.toList()
		return permissionList
	}

	List<Permission> findPermissionsToTransfer() {
		return Permission.withCriteria {
			isNotNull "invite"
		}
	}

	/**
	 * Get all resources of given type that the user has specified permission for
	 */
	def <T> List<T> get(Class<T> resourceClass, Userish userish, Permission.Operation op, boolean includeAnonymous,
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
	Closure createUserPermissionCriteria(Class resourceClass, Userish userish, Permission.Operation op, boolean includeAnonymous) {
		userish = userish?.resolveToUserish()

		boolean isUser = isNotNullAndIdNotNull(userish)
		String userProp = isUser ? PermissionService.getUserPropertyName(userish) : null
		String idProperty = PermissionService.getResourcePropertyName(resourceClass)

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
}
