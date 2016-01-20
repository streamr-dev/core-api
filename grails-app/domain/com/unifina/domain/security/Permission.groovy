package com.unifina.domain.security

/**
 * Access Control List (ACL) item, grants a user a specific type of access to a resource (e.g. X can read Dashboard 1)
 */
class Permission {

	static belongsTo = [user: SecUser]

	/** class of the resource, e.g. "Dashboard" */
	String clazz

	// either stringId (UUID) or longId (autoincrement or similar) is used to refer to a resource, depending on resource type
	String stringId
	Long longId

	/** type of operation that this ACL item allows e.g. "read" */
	String operation

	static constraints = {
		stringId(nullable: true)
		longId(nullable: true)
	}
}
