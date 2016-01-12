package com.unifina.domain.security

/**
 * Access Control List (ACL) item, grants a user a specific type of access to a resource (e.g. X can read Dashboard 1)
 */
class Permission {

	SecUser user

	/** class of the resource, e.g. "Dashboard" */
	String clazz

	String stringId	// UUID
	long longId

	/** type of operation that this ACL item allows e.g. "read" */
	String operation

	static constraints = {
	}
}
