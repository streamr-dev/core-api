package com.unifina.service

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.Userish
import groovy.transform.CompileStatic

class PermissionStore {
	/** null is often a valid value (but not a valid user), and means "anonymous Permissions only" */
	private static boolean isNotNullAndIdNotNull(Userish userish) {
		return userish != null && userish.id != null
	}

	List<Permission> findDirectPermissionsForGetPermissionsTo(String resourceProp, resource, Userish userish) {
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
		return directPermissions
	}

	List<Permission> findDirectPermissionsForHasPermission(String resourceProp, resource, Permission.Operation op, Userish userish) {
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
		Integer n = Permission.createCriteria().count {
			eq(resourceProp, resource)
			eq("operation", Permission.Operation.shareOperation(resource))
		}
		return n
	}

	List<Permission> findPermissionsToRevoke(String resourceProp, resource, boolean anonymous, Userish target) {
		List<Permission> permissionList = Permission.withCriteria {
			eq(resourceProp, resource)
			if (anonymous) {
				eq("anonymous", true)
			} else {
				String userProp = getUserPropertyName(target)
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
	 * Creates a criteria that can be included in the <code>BuildableCriteria</code> of a domain object
	 * (Dashboard, Canvas, Stream etc.) to filter query results so that user has specified permission on
	 * them.
	 */
	Closure createUserPermissionCriteria(Class resourceClass, Userish userish, Permission.Operation op, boolean includeAnonymous) {
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
}
