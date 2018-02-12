package com.unifina.domain.security

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.ModulePackage

/**
 * Access Control List (ACL) item, grants a user a specific type of access to a resource (e.g. X can read Dashboard 1)
 */
class Permission {

	/** Permission can be global, that is, let (also) anonymous users execute the operation */
	Boolean anonymous = false

	/** Permission "belongs to" either a SecUser, Key, or (transiently) a SignupInvite. Ignored for anonymous Permissions */
	SecUser user
	Key key
	SignupInvite invite

	/** Permission is given to one of the resources below */
	Canvas canvas
	Dashboard dashboard
	Feed feed
	ModulePackage modulePackage
	Stream stream

	/** Type of operation that this ACL item allows e.g. "read" */
	enum Operation {
		READ("read"),
		WRITE("write"),
		SHARE("share")

		String id

		Operation(String id) {
			this.id = id
		}

		static fromString(String operationId) {
			return Operation.enumConstants.find { it.id == operationId }
		}
	}
	Operation operation = Operation.READ

	static belongsTo = [Canvas, Dashboard, Feed, ModulePackage, Stream]

	static constraints = {
		user(nullable: true)
		key(nullable: true)
		invite(nullable: true)
		canvas(nullable: true)
		dashboard(nullable: true)
		feed(nullable: true)
		modulePackage(nullable: true)
		stream(nullable: true)
		canvas(validator: { val, obj ->
			[obj.canvas, obj.dashboard, obj.feed, obj.modulePackage, obj.stream].count { it != null } == 1
		})
	}

	static mapping = {
		anonymous(index: 'anonymous_idx')
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
		} else if (user || invite) {
			return [
					id: id,
					user: user?.username ?: invite?.username,
					operation: operation.id
			]
		} else if (key) {
			return [
					id: id,
					key: key.toMap(),
					operation: operation.id
			]
		} else {
			throw new IllegalStateException("Invalid Permission! Must relate to one of: anonymous, user, invite, key")
		}
	}
}
