import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas

class UrlMappings {
	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(controller:"canvas")

		// 403 would be handled by Spring Security Core by default, but due to
		// https://jira.grails.org/browse/GPSPRINGSECURITYCORE-253 it needs to be specified explicitly
		"403"(controller: "login", action: "denied")
		"500"(controller: "error", action: "index", exception: Exception)

		"/login/auth"(controller: "auth", action: "index")
		"/login/full"(controller: "auth", action: "fullAuth")
		"/register/$action**?"(controller: "auth", action: "index")

		"/webcomponents/$view"(controller: "webcomponents", action: "index")

		// API v1 url mappings
		"/api/v1/signups"(controller: "authApi", action: "signup")
		"/api/v1/users"(controller: "authApi", action: "register")
		"/api/v1/passwords/tokens"(controller: "authApi", action: "forgotPassword")
		"/api/v1/passwords"(controller: "authApi", action: "resetPassword")

		"/api/v1/canvases"(resources: "canvasApi", excludes: ["create", "edit"])
		"/api/v1/canvases/$id/start"(controller: "canvasApi", action: "start")
		"/api/v1/canvases/$id/startAsAdmin"(controller: "canvasApi", action: "startAsAdmin")
		"/api/v1/canvases/$id/stop"(controller: "canvasApi", action: "stop")
		"/api/v1/canvases/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Canvas }
		"/api/v1/canvases/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Canvas }
		"/api/v1/canvases/$canvasId/modules/$moduleId"(controller: "canvasApi", action: "module") // for internal use

		"/api/v1/streams"(resources: "streamApi", excludes: ["create", "edit"])
		"/api/v1/streams/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Stream }
		"/api/v1/streams/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Stream }
		"/api/v1/streams/$id/fields"(controller: "streamApi", action: "setFields")
		"/api/v1/streams/$id/detectFields"(controller: "streamApi", action: "detectFields")
		"/api/v1/streams/$id/range"(controller: "streamApi", action: "range")
		"/api/v1/streams/$id/uploadCsvFile"(controller: "streamApi", action: "uploadCsvFile")
		"/api/v1/streams/$id/confirmCsvFileUpload"(controller: "streamApi", action: "confirmCsvFileUpload")
		"/api/v1/streams/$id/dataFiles"(controller: "streamApi", action: "dataFiles")
		"/api/v1/streams/$id/publishers"(controller: "streamApi", action: "publishers")
		"/api/v1/streams/$resourceId/keys"(resources: "keyApi", excludes: ["create", "edit", "update"]) { resourceClass = Stream }

		"/api/v1/dashboards"(resources: "dashboardApi", excludes: ["create", "edit"])
		"/api/v1/dashboards/$dashboardId/items"(resources: "dashboardItemApi", excludes: ["create", "edit"])
		"/api/v1/dashboards/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Dashboard }
		"/api/v1/dashboards/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Dashboard }

		"/api/v1/metrics"(resources: "metricsApi")

		"/api/v1/modules"(resources: "moduleApi")
		"/api/v1/modules/$id/help"(controller: "moduleApi", action: "help")

		"/api/v1/users/me"(controller: "userApi", action: "getUserInfo")
		"/api/v1/users/me/keys"(resources: "keyApi", excludes: ["create", "edit", "update"]) { resourceClass = SecUser }
		"/api/v1/users/me/products"(controller: "productApi", action: "index") { operation = Permission.Operation.SHARE }

		"/api/v1/integration_keys"(resources: "integrationKeyApi")

		"/api/v1/canvases/($path**)/request"(controller: "canvasApi", action: "runtimeRequest") // for internal use, runtime requests to canvases
		"/api/v1/dashboards/($path**)/request"(controller: "dashboardApi", action: "runtimeRequest") // for internal use, runtime requests to canvases via dashboards

		"/api/v1/oembed"(controller: "oembedApi", action: "index")

		"/api/v1/login/challenge/$address"(controller: "loginApi", action: "challenge")
		"/api/v1/login/$action"(controller: "loginApi")

		"/api/v1/categories"(resources: "categoryApi")

		"/api/v1/products"(resources: "productApi")
		"/api/v1/products/$productId/streams"(resources: "productStreamsApi")
		"/api/v1/products/$id/$action"(controller: "productApi")
		"/api/v1/products/$id/images"(controller: "productApi", action: "uploadImage")
		"/api/v1/products/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Product }
		"/api/v1/products/remove/$username"(controller: "removeUsersProducts", action: "index", method: "DELETE")
		"/api/v1/products/$id/related"(controller: "productApi", action: "related", method: "GET")
		"/api/v1/products/stale"(controller: "productApi", action: "staleProducts", method: "GET")

		"/api/v1/subscriptions"(resources: "subscriptionApi")

		"/api/v1/nodes"(controller: "nodeApi", action: "index")
		"/api/v1/nodes/ip"(controller: "nodeApi", action: "ip")
		"/api/v1/nodes/config"(controller: "nodeApi", action: "config")
		"/api/v1/nodes/shutdown"(controller: "nodeApi", action: "shutdown")
		"/api/v1/nodes/canvases"(controller: "nodeApi", action: "canvases")
		"/api/v1/nodes/canvases/sizes"(controller: "nodeApi", action: "canvasSizes")
		"/api/v1/nodes/$nodeIp/config"(controller: "nodeApi", action: "configNode")
		"/api/v1/nodes/$nodeIp/shutdown"(controller: "nodeApi", action: "shutdownNode")
		"/api/v1/nodes/$nodeIp/canvases"(controller: "nodeApi", action: "canvasesNode")

		"/api/v1/cluster/$action"(controller: "clusterApi")

		// Mappings for pages using React Router (the root for the router)
		"/dashboard/editor/$id**?"(controller: "dashboard", action: "editor")
	}
}
