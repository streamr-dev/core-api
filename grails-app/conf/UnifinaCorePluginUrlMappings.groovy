import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas

class UnifinaCorePluginUrlMappings {
	static mappings = {
		"/localFeedFile/$feedDir/$day/$file"(controller:"localFeedFile",action:"index")
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		// Root should be mapped in app, not in plugin, because plugin url mappings can't currently be overridden
		
		// 403 would be handled by Spring Security Core by default, but due to 
		// https://jira.grails.org/browse/GPSPRINGSECURITYCORE-253 it needs to be specified explicitly
		"403"(controller: "login", action: "denied")
		"500"(controller: "error", action: "index", exception: Exception)
		
		"/webcomponents/$view"(controller: "webcomponents", action: "index")

		// API v1 url mappings
		"/api/v1/canvases"(resources: "canvasApi", excludes: ["create", "edit"])
		"/api/v1/canvases/$id/start"(controller: "canvasApi", action: "start")
		"/api/v1/canvases/$id/stop"(controller: "canvasApi", action: "stop")
		"/api/v1/canvases/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Canvas }
		"/api/v1/canvases/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Canvas }
		"/api/v1/canvases/$canvasId/modules/$moduleId"(controller: "canvasApi", action: "module") // for internal use

		"/api/v1/streams"(resources: "streamApi", excludes: ["create", "edit"])
		"/api/v1/streams/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Stream }
		"/api/v1/streams/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Stream }
		"/api/v1/streams/$id/detectFields"(controller: "streamApi", action: "detectFields")
		"/api/v1/streams/$id/range"(controller: "streamApi", action: "range")
		"/api/v1/streams/$resourceId/keys"(resources: "keyApi", excludes: ["create", "edit", "update"]) { resourceClass = Stream }

		"/api/v1/dashboards"(resources: "dashboardApi", excludes: ["create", "edit"])
		"/api/v1/dashboards/$dashboardId/items"(resources: "dashboardItemApi", excludes: ["create", "edit"])
		"/api/v1/dashboards/$resourceId/permissions"(resources: "permissionApi", excludes: ["create", "edit", "update"]) { resourceClass = Dashboard }
		"/api/v1/dashboards/$resourceId/permissions/me"(controller: "permissionApi", action: "getOwnPermissions") { resourceClass = Dashboard }

		"/api/v1/modules"(resources: "moduleApi")
		"/api/v1/modules/$id/help"(controller: "moduleApi", action: "help")

		"/api/v1/users/me"(controller: "userApi", action: "getUserInfo")
		"/api/v1/users/me/keys"(resources: "keyApi", excludes: ["create", "edit", "update"]) { resourceClass = SecUser }

		"/api/v1/integration_keys"(resources: "integrationKeyApi")

		"/api/v1/canvases/($path**)/request"(controller: "canvasApi", action: "runtimeRequest") // for internal use, runtime requests to canvases
		"/api/v1/dashboards/($path**)/request"(controller: "dashboardApi", action: "runtimeRequest") // for internal use, runtime requests to canvases via dashboards

		// Mappings for pages using React Router (the root for the router)
		"/dashboard/editor/$id**?"(controller: "dashboard", action: "editor")
	}
}