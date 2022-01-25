package com.streamr.core.domain

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

/**
 * Access Control List (ACL) item, grants a user a specific type of access to a resource (e.g. X can read Product 1)
 *
 * EqualsAndHashCode are used in PermissionService to build a Set in a special case of UI Channels.
 * @see com.streamr.core.service.PermissionService#getPermissionsTo(Product, User)
 */
@EqualsAndHashCode(includes = "anonymous,user,product,operation,subscription,endsAt,parent")
@Entity
class Permission {

	/** Permission can be global, that is, let (also) anonymous users execute the operation */
	Boolean anonymous = false

	/** Permission "belongs to" a User. Ignored for anonymous Permissions */
	User user

	/**
	 * Permission is given to one of the resources below. To add new types:
	 * 1) Define the field: MUST use the camelCase version of the class name
	 * 2) Add the field to the resourceFields list
	 */
	Product product

	/** Type of operation that this ACL item allows e.g. "read" */
	enum Operation {
		// Open product
		PRODUCT_GET("product_get"),
		// Edit product
		PRODUCT_EDIT("product_edit"),
		// Delete product
		PRODUCT_DELETE("product_delete"),
		// Edit user permissions to product
		PRODUCT_SHARE("product_share")

		String id

		Operation(String id) {
			this.id = id
		}

		static Operation fromString(String operationId) {
			if (operationId == null || "".equals(operationId)) {
				throw new IllegalArgumentException("Permission operation cannot be null or empty.")
			}
			operationId = operationId.toUpperCase()
			return Operation.valueOf(operationId)
		}

		static List<Permission.Operation> productOperations() {
			return [
				PRODUCT_GET,
				PRODUCT_EDIT,
				PRODUCT_DELETE,
				PRODUCT_SHARE,
			]
		}

		/**
		 * Method expects input in upper case.
		 *
		 * @return {@code true} if a valid operation and {@code false} otherwise.
		 */
		static boolean validateOperation(final String op) {
			if (op == null) {
				return false
			}
			try {
				Operation.valueOf(op)
			} catch (IllegalArgumentException e) {
				return false
			}
			return true
		}
	}

	Operation operation

	/** Is this a Permission of a Subscription? **/
	Subscription subscription
	/** When does this Permission expire? null == forever valid */
	Date endsAt
	/** This permission may have been created due to another permission, keep track */
	Permission parent

	static belongsTo = [
		Subscription,
	]

	static constraints = {
		user(nullable: true)
		product(nullable: true)
		subscription(nullable: true)
		endsAt(nullable: true)
		parent(nullable: true)
	}

	/**
	 * Client-side representation of Permission object
	 * Resource type/id is not indicated because API caller will have it in the URL
	 * @return map to be shown to the API callers
	 */
	Map toMap() {
		if (anonymous) {
			return [
				id: id,
				anonymous: true,
				operation: operation.id
			]
		} else if (user) {
			return [
				id: id,
				user: user?.username,
				operation: operation.id
			]
		} else {
			throw new IllegalStateException("Invalid Permission! Must relate to one of: anonymous or user")
		}
	}

	Map toInternalMap() {
		Map map = [
			operation: operation.toString(),
			subscription: subscription?.id
		]
		if (anonymous) {
			map["anonymous"] = true
		}
		if (user) {
			map["user"] = user.id
		}
		if (product) {
			map["product"] = product.id
		}
		if (endsAt) {
			map["endsAt"] = endsAt
		}
		if (parent) {
			map["parent"] = parent.id
		}
		return map
	}

	@Override
	String toString() {
		return toInternalMap().toString()
	}
}
