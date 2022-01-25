class UrlMappings {
	static mappings = {
		"500"(controller: "error", action: "index", exception: Exception)
		"404"(controller: "error", action: "notFound")

		"/api/v2/permissions/cleanup"(method: "DELETE", controller: "permissionApi", action: "cleanup")

		"/api/v2/metrics"(resources: "metricsApi")

		"/api/v2/users/me"(method: "GET", controller: "userApi", action: "getUserInfo")
		"/api/v2/users/me"(method: "PUT", controller: "userApi", action: "update")
		"/api/v2/users/me"(method: "DELETE", controller: "userApi", action: "delete")

		"/api/v2/users/me/products"(method: "GET", controller: "productApi", action: "index")
		"/api/v2/users/me/image"(method: "POST", controller: "userApi", action: "uploadAvatarImage")
		"/api/v2/users/me/balance"(method: "GET", controller: "userApi", action: "getCurrentUserBalance")

		"/api/v2/login/challenge/$address"(method: "POST", controller: "loginApi", action: "challenge")
		"/api/v2/login/response"(method: "POST", controller: "loginApi", action: "response")
		"/api/v2/logout"(method: "POST", controller: "logoutApi", action: "logout")

		"/api/v2/categories"(resources: "categoryApi")

		"/api/v2/products"(resources: "productApi")
		"/api/v2/products/$productId/streams"(resources: "productStreamsApi")
		"/api/v2/products/$id/$action"(controller: "productApi")
		"/api/v2/products/$id/images"(method: "POST", controller: "productApi", action: "uploadImage")
		"/api/v2/products/$productId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"])
		"/api/v2/products/$productId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions")
		"/api/v2/products/remove/$username"(method: "DELETE", controller: "removeUsersProducts", action: "index")
		"/api/v2/products/$id/related"(method: "GET", controller: "productApi", action: "related")

		"/api/v2/subscriptions"(resources: "subscriptionApi")

		"/api/v2/nodes/config"(method: "GET", controller: "nodeApi", action: "config")

		"/api/v2/dataunions/$contractAddress/joinRequests"(resources: "dataUnionJoinRequestApi", excludes: ["create", "edit"])
		"/api/v2/dataunions/$contractAddress/secrets"(resources: "dataUnionSecretApi", excludes: ["create", "edit"])
	}
}
