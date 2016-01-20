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
		"500"(view:'/error')
		
		"/webcomponents/$view"(controller: "webcomponents", action: "index")

		// API v1 url mappings
		"/api/v1/canvases"(resources: "canvasApi", excludes: ["create", "edit"])
		"/api/v1/streams"(resources: "streamApi", excludes: ["create", "edit"])

		"/api/v1/live"(resources: "liveApi", excludes: ["create", "edit"])
		"/api/v1/live/request"(controller: "liveApi", action: "request")
		"/api/v1/live/getModuleJson"(controller: "liveApi", action: "getModuleJson")
		"/api/v1/live/ajaxCreate"(controller: "liveApi", action: "ajaxCreate")
		"/api/v1/live/ajaxStop"(controller: "liveApi", action: "ajaxStop")
	}
}