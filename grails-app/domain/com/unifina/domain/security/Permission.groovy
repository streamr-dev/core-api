package com.unifina.domain.security

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.Subscription
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

	/**
	 * Permission is given to one of the resources below. To add new types:
	 * 1) Define the field: MUST use the camelCase version of the class name
	 * 2) Add the field to the resourceFields list
	 */
	Canvas canvas
	Dashboard dashboard
	Feed feed
	ModulePackage modulePackage
	Stream stream
	Product product
	static List<String> resourceFields = ['canvas', 'dashboard', 'feed', 'modulePackage', 'stream', 'product']

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

	/** Is this a Permission of a Subscription? **/
	Subscription subscription
	/** When does this Permission expire? null == forever valid */
	Date endsAt

	static belongsTo = [Canvas, Dashboard, Feed, ModulePackage, Stream, Subscription]

	static constraints = {
		user(nullable: true)
		key(nullable: true)
		invite(nullable: true)
		canvas(nullable: true)
		dashboard(nullable: true)
		feed(nullable: true)
		modulePackage(nullable: true)
		stream(nullable: true)
		product(nullable: true)
		canvas(validator: { val, obj ->
			[obj.canvas, obj.dashboard, obj.feed, obj.modulePackage, obj.stream, obj.product].count { it != null } == 1
		})
		subscription(nullable: true)
		endsAt(nullable: true)
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
		if (key) {
			map["key"] = key.id
		}
		if (invite) {
			map["invite"] = invite.id
		}
		if (canvas) {
			map["canvas"] = canvas.id
		}
		if (dashboard) {
			map["dashboard"] = dashboard.id
		}
		if (feed) {
			map["feed"] = feed.id
		}
		if (modulePackage) {
			map["modulePackage"] = modulePackage.id
		}
		if (stream) {
			map["stream"] = stream.id
		}
		if (product) {
			map["product"] = product.id
		}
		if (endsAt) {
			map["endsAt"] = endsAt
		}
		return map
	}
}
