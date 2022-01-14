package com.unifina.service

import com.unifina.domain.Permission
import com.unifina.domain.Product
import com.unifina.domain.User

class PermissionStore {
	/** null is often a valid value (but not a valid user), and means "anonymous Permissions only" */
	private static boolean isNotNullAndIdNotNull(User user) {
		return user != null && user.id != null
	}

	List<Permission> findDirectPermissions(Product resource, Permission.Operation operation, User user) {
		List<Permission> directPermissions = new LinkedList<>();

		// first find only user-specific Permissions (100..1000x faster than "anonymous OR user-specific" query)
		if (isNotNullAndIdNotNull(user)) {
			directPermissions = Permission.withCriteria {
				eq("product", resource)
				if (operation != null) {
					eq("operation", operation)
				}
				eq("user", user)
				or {
					isNull("endsAt")
					gt("endsAt", new Date())
				}
			}
		}

		// if no user-specific permissions found, do the slower anonymous permissions query (still 10x faster than "OR" query)
		if (directPermissions.isEmpty()) {
			directPermissions = Permission.withCriteria {
				eq("product", resource)
				if (operation != null) {
					eq("operation", operation)
				}
				eq("anonymous", true)
				or {
					isNull("endsAt")
					gt("endsAt", new Date())
				}
			}
		}

		return directPermissions
	}

	List<Permission> getPermissionsTo(Product resource, boolean subscriptions, Permission.Operation op) {
		List<Permission> results = Permission.createCriteria().list() {
			eq("product", resource)
			if (!subscriptions) {
				eq('subscription', null)
			}
			if (op) {
				eq('operation', op)
			}
		}
		return results
	}

	int countSharePermissions(Product resource) {
		Integer n = Permission.createCriteria().count {
			eq("product", resource)
			eq("operation", Permission.Operation.PRODUCT_SHARE)
		}
		return n
	}

	List<Permission> findPermissionsToRevoke(Product resource, boolean anonymous, User target) {
		List<Permission> permissionList = Permission.withCriteria {
			eq("product", resource)
			if (anonymous) {
				eq("anonymous", true)
			} else {
				eq("user", target)
			}
		}.toList()
		return permissionList
	}

	/**
	 * Get all resources of given type that the user has specified permission for
	 */
	List<Product> get(User user, Permission.Operation op, boolean includeAnonymous, Closure resourceFilter = {}) {
		if (!includeAnonymous && !user?.id) {
			return []
		}

		Closure permissionCriteria = createUserPermissionCriteria(user, op, includeAnonymous)

		return Product.withCriteria {
			permissionCriteria.delegate = delegate
			resourceFilter.delegate = delegate
			permissionCriteria()
			resourceFilter()
		}
	}

	/**
	 * Creates a criteria that can be included in the <code>BuildableCriteria</code> of a domain object
	 * Product to filter query results so that user has specified permission on them.
	 */
	Closure createUserPermissionCriteria(User user, Permission.Operation op, boolean includeAnonymous) {
		boolean isUser = isNotNullAndIdNotNull(user)

		return {
			permissions {
				eq("operation", op)
				or {
					if (includeAnonymous) {
						eq("anonymous", true)
					}
					if (isUser) {
						eq("user", user)
					}
				}
				or {
					isNull("endsAt")
					gt("endsAt", new Date())
				}
				projections {
					groupProperty("product")
				}
			}
		}
	}
}
