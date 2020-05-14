package com.unifina.domain.security

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.Subscription
import com.unifina.domain.signalpath.Canvas
import grails.persistence.Entity
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

/**
 * Access Control List (ACL) item, grants a user a specific type of access to a resource (e.g. X can read Dashboard 1)
 *
 * EqualsAndHashCode are used in PermissionService to build a Set in a special case of UI Channels.
 * @see com.unifina.service.PermissionService#getPermissionsTo(Object r, com.unifina.security.Userish u)
 */
@EqualsAndHashCode(includes="anonymous,user,key,invite,canvas,dashboard,stream,product,operation,subscription,endsAt,parent")
@Entity
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
	Stream stream
	Product product
	static List<String> resourceFields = ['canvas', 'dashboard', 'stream', 'product']

	/** Type of operation that this ACL item allows e.g. "read" */
	enum Operation {
		/*
			Stream

			read -> get, subscribe
			write -> edit, publish, delete
			share -> share
		*/
		// Fetch stream details
		STREAM_GET("stream_get"),
		// Edit stream details
		STREAM_EDIT("stream_edit"),
		// Delete stream
		STREAM_DELETE("stream_delete"),
		// Publish to stream
		STREAM_PUBLISH("stream_publish"),
		// Subscribe to stream
		STREAM_SUBSCRIBE("stream_subscribe"),
		// Edit user permissions to stream
		STREAM_SHARE("stream_share"),

		/*
			Canvas

			read -> get, interact
			write -> edit, startstop, delete
			share -> share
		*/
		// Open canvas
		CANVAS_GET("canvas_get"),
		// Edit canvas
		CANVAS_EDIT("canvas_edit"),
		// Delete canvas
		CANVAS_DELETE("canvas_delete"),
		// Start and stop canvas
		CANVAS_STARTSTOP("canvas_startstop"),
		// Interact with runtime widgets (buttons, switches, etc.)
		CANVAS_INTERACT("canvas_interact"),
		// Edit user permissions to canvas
		CANVAS_SHARE("canvas_share"),

		/*
			Dasboard

			read -> get, interact
			write -> edit, delete
			share -> share
		*/
		// Open dashboard
		DASHBOARD_GET("dashboard_get"),
		// Edit dashboard
		DASHBOARD_EDIT("dashboard_edit"),
		// Delete dashboard
		DASHBOARD_DELETE("dashboard_delete"),
		// Interact with runtime widgets (buttons, switches, etc.)
		DASHBOARD_INTERACT("dashboard_interact"),
		// Edit user permissions to dashboard
		DASHBOARD_SHARE("dashboard_share"),

		/*
			Product

			read -> get
			write -> edit, delete
			share -> share
		*/
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

		@CompileStatic
		static List<Permission.Operation> operationsFor(Object resource) {
			if (resource == null) {
				throw new IllegalArgumentException("Unknown resource: " + resource)
			}
			Class<?> resourceClass = resource.getClass()
			if (Canvas.isAssignableFrom(resourceClass)) {
				return canvasOperations()
			} else if (Stream.isAssignableFrom(resourceClass)) {
				return streamOperations()
			} else if (Dashboard.isAssignableFrom(resourceClass)) {
				return dashboardOperations()
			} else if (Product.isAssignableFrom(resourceClass)) {
				return productOperations()
			}
			throw new IllegalArgumentException("Unknown resource: " + resource)
		}

		@CompileStatic
		static Operation shareOperation(Object resource) {
			if (resource == null) {
				throw new IllegalArgumentException("Unknown resource: " + resource)
			}
			Class<?> resourceClass = resource.getClass()
			if (Canvas.isAssignableFrom(resourceClass)) {
				return CANVAS_SHARE
			} else if (Stream.isAssignableFrom(resourceClass)) {
				return STREAM_SHARE
			} else if (Dashboard.isAssignableFrom(resourceClass)) {
				return DASHBOARD_SHARE
			} else if (Product.isAssignableFrom(resourceClass)) {
				return PRODUCT_SHARE
			}
			throw new IllegalArgumentException("Unknown resource: " + resource)
		}

		static List<Permission.Operation> shareOperations() {
			return [
				CANVAS_SHARE,
				DASHBOARD_SHARE,
				PRODUCT_SHARE,
				STREAM_SHARE,
			]
		}

		static List<Permission.Operation> streamOperations() {
			return [
				STREAM_GET,
				STREAM_EDIT,
				STREAM_DELETE,
				STREAM_PUBLISH,
				STREAM_SUBSCRIBE,
				STREAM_SHARE,
			]
		}
		static List<Permission.Operation> canvasOperations() {
			return [
				CANVAS_GET,
				CANVAS_EDIT,
				CANVAS_DELETE,
				CANVAS_STARTSTOP,
				CANVAS_INTERACT,
				CANVAS_SHARE,
			]
		}
		static List<Permission.Operation> dashboardOperations() {
			return [
				DASHBOARD_GET,
				DASHBOARD_EDIT,
				DASHBOARD_DELETE,
				DASHBOARD_INTERACT,
				DASHBOARD_SHARE,
			]
		}
		static List<Permission.Operation> productOperations() {
			return [
				PRODUCT_GET,
				PRODUCT_EDIT,
				PRODUCT_DELETE,
				PRODUCT_SHARE,
			]
		}
	}

	Operation operation

	/** Is this a Permission of a Subscription? **/
	Subscription subscription
	/** When does this Permission expire? null == forever valid */
	Date endsAt
	/** This permission may have been created due to another permission, keep track */
	Permission parent

	static belongsTo = [Canvas, Dashboard, Stream, Subscription]

	static constraints = {
		user(nullable: true)
		key(nullable: true)
		invite(nullable: true)
		canvas(nullable: true)
		dashboard(nullable: true)
		stream(nullable: true)
		product(nullable: true)
		canvas(validator: { val, obj ->
			[obj.canvas, obj.dashboard, obj.stream, obj.product].count { it != null } == 1
		})
		subscription(nullable: true)
		endsAt(nullable: true)
		parent(nullable: true)
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
		if (stream) {
			map["stream"] = stream.id
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
