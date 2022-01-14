class UrlMappings {
	static mappings = {
		"500"(controller: "error", action: "index", exception: Exception)
		"404"(controller: "error", action: "notFound")

		"/api/v1/permissions/cleanup"(method: "DELETE", controller: "permissionApi", action: "cleanup")

		"/api/v1/metrics"(resources: "metricsApi")

		"/api/v1/users/me"(method: "GET", controller: "userApi", action: "getUserInfo")
		"/api/v1/users/me"(method: "PUT", controller: "userApi", action: "update")
		"/api/v1/users/me"(method: "DELETE", controller: "userApi", action: "delete")

		"/api/v1/users/me/products"(method: "GET", controller: "productApi", action: "index")
		"/api/v1/users/me/image"(method: "POST", controller: "userApi", action: "uploadAvatarImage")
		"/api/v1/users/me/balance"(method: "GET", controller: "userApi", action: "getCurrentUserBalance")

		"/api/v1/login/challenge/$address"(method: "POST", controller: "loginApi", action: "challenge")
		"/api/v1/login/response"(method: "POST", controller: "loginApi", action: "response")
		"/api/v1/logout"(method: "POST", controller: "logoutApi", action: "logout")

		"/api/v1/categories"(resources: "categoryApi")

		"/api/v1/products"(resources: "productApi")
		"/api/v1/products/$productId/streams"(resources: "productStreamsApi")
		"/api/v1/products/$id/$action"(controller: "productApi")
		"/api/v1/products/$id/images"(method: "POST", controller: "productApi", action: "uploadImage")
		"/api/v1/products/$productId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"])
		"/api/v1/products/$productId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions")
		"/api/v1/products/remove/$username"(method: "DELETE", controller: "removeUsersProducts", action: "index")
		"/api/v1/products/$id/related"(method: "GET", controller: "productApi", action: "related")

		"/api/v1/subscriptions"(resources: "subscriptionApi")

		"/api/v1/nodes/config"(method: "GET", controller: "nodeApi", action: "config")

		"/api/v1/dataunions/$contractAddress/joinRequests"(resources: "dataUnionJoinRequestApi", excludes: ["create", "edit"])
		"/api/v1/dataunions/$contractAddress/secrets"(resources: "dataUnionSecretApi", excludes: ["create", "edit"])
	}
}
