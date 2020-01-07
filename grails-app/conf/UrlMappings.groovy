import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas

class UrlMappings {
	static mappings = {
		"500"(controller: "error", action: "index", exception: Exception)

		// API v1 url mappings
		"/api/v1/signups"(method: "POST", controller: "authApi", action: "signup")
		"/api/v1/users"(method: "POST", controller: "authApi", action: "register")
		"/api/v1/passwords/tokens"(method: "POST", controller: "authApi", action: "forgotPassword")
		"/api/v1/passwords"(method: "POST", controller: "authApi", action: "resetPassword")

		"/api/v1/canvases"(resources: "canvasApi", excludes: ["create", "edit"])
		"/api/v1/canvases/$id/start"(controller: "canvasApi", action: "start")
		"/api/v1/canvases/$id/startAsAdmin"(controller: "canvasApi", action: "startAsAdmin")
		"/api/v1/canvases/$id/stop"(controller: "canvasApi", action: "stop")
		"/api/v1/canvases/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Canvas }
		"/api/v1/canvases/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Canvas }
		"/api/v1/canvases/$canvasId/modules/$moduleId"(controller: "canvasApi", action: "module") // for internal use
		"/api/v1/canvases/downloadCsv"(method: "GET", controller: "canvasApi", action: "downloadCsv")

		"/api/v1/streams"(resources: "streamApi", excludes: ["create", "edit"])
		"/api/v1/streams/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Stream }
		"/api/v1/streams/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Stream }
		"/api/v1/streams/$id/fields"(method: "POST", controller: "streamApi", action: "setFields")
		"/api/v1/streams/$id/detectFields"(method: "POST", controller: "streamApi", action: "detectFields")
		"/api/v1/streams/$id/detectFields"(method: "GET", controller: "streamApi", action: "detectFields")
		"/api/v1/streams/$id/range"(controller: "streamApi", action: "range")
		"/api/v1/streams/$id/uploadCsvFile"(method: "POST", controller: "streamApi", action: "uploadCsvFile")
		"/api/v1/streams/$id/confirmCsvFileUpload"(method: "POST", controller: "streamApi", action: "confirmCsvFileUpload")
		"/api/v1/streams/$id/dataFiles"(controller: "streamApi", action: "dataFiles")
		"/api/v1/streams/$id/publishers"(controller: "streamApi", action: "publishers")
		"/api/v1/streams/$id/publisher/$address"(controller: "streamApi", action: "publisher")
		"/api/v1/streams/$id/subscribers"(controller: "streamApi", action: "subscribers")
		"/api/v1/streams/$id/subscriber/$address"(controller: "streamApi", action: "subscriber")
		"/api/v1/streams/$id/status"(controller: "streamApi", action: "status")
		"/api/v1/streams/$resourceId/keys"(resources: "keyApi", excludes: ["create", "edit", "update"]) { resourceClass = Stream }
		"/api/v1/streams/$streamId/keys/$keyId"(method: "PUT", controller: "keyApi", action: "updateStreamKey")
		"/api/v1/streams/$id/deleteDataUpTo"(method: "DELETE", controller: "streamApi", action: "deleteDataUpTo")
		"/api/v1/streams/$id/deleteAllData"(method: "DELETE", controller: "streamApi", action: "deleteAllData")
		"/api/v1/streams/$id/deleteDataRange"(method: "DELETE", controller: "streamApi", action: "deleteDataRange")

		"/api/v1/dashboards"(resources: "dashboardApi", excludes: ["create", "edit"])
		"/api/v1/dashboards/$dashboardId/items"(resources: "dashboardItemApi", excludes: ["create", "edit"])
		"/api/v1/dashboards/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Dashboard }
		"/api/v1/dashboards/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Dashboard }

		"/api/v1/permissions/cleanup"(method: "DELETE", controller: "permissionApi", action: "cleanup")

		"/api/v1/metrics"(resources: "metricsApi")

		"/api/v1/modules"(resources: "moduleApi")
		"/api/v1/modules/$id/help"(controller: "moduleApi", action: "help")
		"/api/v1/modules/$id"(controller: "moduleApi", action: "jsonGetModule")
		"/api/v1/module_categories"(controller: "moduleApi", action: "jsonGetModuleTree")

		"/api/v1/users/me"(method: "GET", controller: "userApi", action: "getUserInfo")
		"/api/v1/users/me"(method: "PUT", controller: "userApi", action: "update")
		"/api/v1/users/me"(method: "DELETE", controller: "userApi", action: "delete")

		"/api/v1/users/me/keys"(resources: "keyApi", excludes: ["create", "edit", "update"]) { resourceClass = SecUser }
		"/api/v1/users/me/keys/$keyId"(method: "PUT", controller: "keyApi", action: "updateUserKey")
		"/api/v1/users/me/products"(method: "GET", controller: "productApi", action: "index") { operation = Permission.Operation.SHARE }
		"/api/v1/users/me/changePassword"(method: "POST", controller: "userApi", action: "changePassword")
		"/api/v1/users/me/image"(method: "POST", controller: "userApi", action: "uploadAvatarImage")
		"/api/v1/users/me/balance"(method: "GET", controller: "userApi", action: "getCurrentUserBalance")

		"/api/v1/integration_keys"(resources: "integrationKeyApi")

		"/api/v1/canvases/($path**)/request"(controller: "canvasApi", action: "runtimeRequest") // for internal use, runtime requests to canvases
		"/api/v1/dashboards/($path**)/request"(controller: "dashboardApi", action: "runtimeRequest") // for internal use, runtime requests to canvases via dashboards

		"/api/v1/oembed"(controller: "oembedApi", action: "index")

		"/api/v1/login/challenge/$address"(method: "POST", controller: "loginApi", action: "challenge")
		"/api/v1/login/$action"(method: "POST", controller: "loginApi")
		"/api/v1/logout"(method: "POST",  controller: "logoutApi", action: "logout")

		"/api/v1/categories"(resources: "categoryApi")

		"/api/v1/products"(resources: "productApi")
		"/api/v1/products/$productId/streams"(resources: "productStreamsApi")
		"/api/v1/products/$id/$action"(controller: "productApi")
		"/api/v1/products/$id/images"(method: "POST", controller: "productApi", action: "uploadImage")
		"/api/v1/products/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Product }
		"/api/v1/products/remove/$username"(method: "DELETE", controller: "removeUsersProducts", action: "index")
		"/api/v1/products/$id/related"(method: "GET", controller: "productApi", action: "related")
		"/api/v1/products/stale"(method: "GET", controller: "productApi", action: "staleProducts")
		"/api/v1/products/staleEmail"(method: "GET", controller: "productApi", action: "emailStaleProductOwners")

		"/api/v1/subscriptions"(resources: "subscriptionApi")

		"/api/v1/nodes"(method: "GET", controller: "nodeApi", action: "index")
		"/api/v1/nodes/ip"(method: "GET", controller: "nodeApi", action: "ip")
		"/api/v1/nodes/config"(method: "GET", controller: "nodeApi", action: "config")
		"/api/v1/nodes/shutdown"(method: "POST", controller: "nodeApi", action: "shutdown")
		"/api/v1/nodes/canvases"(method: "GET", controller: "nodeApi", action: "canvases")
		"/api/v1/nodes/canvases/sizes"(method: "GET", controller: "nodeApi", action: "canvasSizes")
		"/api/v1/nodes/$nodeIp/config"(method: "GET", controller: "nodeApi", action: "configNode")
		"/api/v1/nodes/$nodeIp/shutdown"(method: "POST", controller: "nodeApi", action: "shutdownNode")
		"/api/v1/nodes/$nodeIp/canvases"(method: "GET", controller: "nodeApi", action: "canvasesNode")

		"/api/v1/cluster/$action"(controller: "clusterApi")

		"/api/v1/communities/$communityAddress/joinRequests"(resources: "communityJoinRequestApi", excludes: ["create", "edit"])
		"/api/v1/communities/$communityAddress/secrets"(resources: "communitySecretApi", excludes: ["create", "edit"])
		"/api/v1/communities/$communityAddress/stats"(method: "GET", controller: "communityOperatorApi", action: "stats")
		"/api/v1/communities/$communityAddress/members"(method: "GET", controller: "communityOperatorApi", action: "members")
		"/api/v1/communities/$communityAddress/members/$memberAddress"(method: "GET", controller: "communityOperatorApi", action: "memberStats")
		"/api/v1/communities"(method: "GET", controller: "communityOperatorApi", action: "summary")
	}
}
