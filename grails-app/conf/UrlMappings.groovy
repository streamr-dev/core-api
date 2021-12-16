import com.unifina.domain.Permission
import com.unifina.domain.Product
import com.unifina.domain.Stream

class UrlMappings {
	static mappings = {
		"500"(controller: "error", action: "index", exception: Exception)
		"404"(controller: "error", action: "notFound")

		// API v1 url mappings
		"/api/v1/signups"(method: "POST", controller: "authApi", action: "signup")
		"/api/v1/users"(method: "POST", controller: "authApi", action: "register")

		"/api/v1/streams"(method: "GET", controller: "streamApi", action: "index")
		"/api/v1/streams"(method: "POST", controller: "streamApi", action: "save")
		"/api/v1/streams/$id"(method: "GET", controller: "streamApi", action: "show")
		"/api/v1/streams/$id"(method: "PUT", controller: "streamApi", action: "update")
		"/api/v1/streams/$id"(method: "DELETE", controller: "streamApi", action: "delete")
		"/api/v1/streams/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Stream }
		"/api/v1/streams/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Stream }
		"/api/v1/streams/$id/fields"(method: "POST", controller: "streamApi", action: "setFields")
		"/api/v1/streams/$id/validation"(method: "GET", controller: "streamApi", action: "validation")
		"/api/v1/streams/$id/publishers"(controller: "streamApi", action: "publishers")
		"/api/v1/streams/$id/publisher/$address"(controller: "streamApi", action: "publisher")
		"/api/v1/streams/$id/subscribers"(controller: "streamApi", action: "subscribers")
		"/api/v1/streams/$id/subscriber/$address"(controller: "streamApi", action: "subscriber")

		"/api/v1/storageNodes/$storageNodeAddress/streams"(method: "GET", controller: "storageNodeApi", action: "findStreamsByStorageNode")
		"/api/v1/streams/$streamId/storageNodes"(method: "GET", controller: "storageNodeApi", action: "findStorageNodesByStream")
		"/api/v1/streams/$streamId/storageNodes"(method: "POST", controller: "storageNodeApi", action: "addStorageNodeToStream")
		"/api/v1/streams/$streamId/storageNodes/$storageNodeAddress"(method: "DELETE", controller: "storageNodeApi", action: "removeStorageNodeFromStream")

		"/api/v1/permissions/cleanup"(method: "DELETE", controller: "permissionApi", action: "cleanup")

		"/api/v1/metrics"(resources: "metricsApi")

		"/api/v1/users/me"(method: "GET", controller: "userApi", action: "getUserInfo")
		"/api/v1/users/me"(method: "PUT", controller: "userApi", action: "update")
		"/api/v1/users/me"(method: "DELETE", controller: "userApi", action: "delete")

		"/api/v1/users/me/products"(method: "GET", controller: "productApi", action: "index") { operation = Permission.Operation.PRODUCT_SHARE }
		"/api/v1/users/me/image"(method: "POST", controller: "userApi", action: "uploadAvatarImage")
		"/api/v1/users/me/balance"(method: "GET", controller: "userApi", action: "getCurrentUserBalance")

		"/api/v1/integration_keys"(resources: "integrationKeyApi")

		"/api/v1/login/challenge/$address"(method: "POST", controller: "loginApi", action: "challenge")
		"/api/v1/login/response"(method: "POST", controller: "loginApi", action: "response")
		"/api/v1/logout"(method: "POST", controller: "logoutApi", action: "logout")

		"/api/v1/categories"(resources: "categoryApi")

		"/api/v1/products"(resources: "productApi")
		"/api/v1/products/$productId/streams"(resources: "productStreamsApi")
		"/api/v1/products/$id/$action"(controller: "productApi")
		"/api/v1/products/$id/images"(method: "POST", controller: "productApi", action: "uploadImage")
		"/api/v1/products/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Product }
		"/api/v1/products/remove/$username"(method: "DELETE", controller: "removeUsersProducts", action: "index")
		"/api/v1/products/$id/related"(method: "GET", controller: "productApi", action: "related")

		"/api/v1/subscriptions"(resources: "subscriptionApi")

		"/api/v1/nodes/config"(method: "GET", controller: "nodeApi", action: "config")

		"/api/v1/dataunions/$contractAddress/joinRequests"(resources: "dataUnionJoinRequestApi", excludes: ["create", "edit"])
		"/api/v1/dataunions/$contractAddress/secrets"(resources: "dataUnionSecretApi", excludes: ["create", "edit"])

		// Deprecated aliases of the above, remove once no one is calling them
		"/api/v1/communities/$contractAddress/joinRequests"(resources: "dataUnionJoinRequestApi", excludes: ["create", "edit"])
		"/api/v1/communities/$contractAddress/secrets"(resources: "dataUnionSecretApi", excludes: ["create", "edit"])
	}
}
