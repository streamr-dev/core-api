package com.unifina.domain.security

/**
 * Access Control List (ACL) item, grants a user a specific type of access to a resource (e.g. X can read Dashboard 1)
 */
class Permission {

	// Permission "belongs to" either a SecUser or (transiently) a SignupInvite
	SecUser user
	SignupInvite invite

	/** full class name of the resource, e.g. "com.unifina.domain.dashboard.Dashboard" */
	String clazz

	// either stringId (UUID) or longId (autoincrement or similar) is used to refer to a resource, depending on resource type
	String stringId
	Long longId

	/** type of operation that this ACL item allows e.g. "read" */
	String operation

	static constraints = {
		stringId(nullable: true)
		longId(nullable: true)
		user(nullable: true)
		invite(nullable: true)
	}

	/**
	 * Client-side representation of Permission object
	 * Resource type/id is not indicated because API caller will have it in the URL
	 * @return map to be shown to the API callers
	 *
     */
	public Map toMap() {[
		id: id,
		user: user?.username ?: invite?.username,
		operation: operation
	]}
}
