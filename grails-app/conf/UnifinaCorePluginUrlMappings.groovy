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
		
		// API url mappings
		//"/api/stream/create"(controller: "stream", action: "apiCreate")
		//"/api/stream/lookup"(controller: "stream", action: "apiLookup")
		"/api/live/request"(controller: "live", action: "request")

		// API v1 url mappings
		"/api/v1/stream"(controller: "streamApi", action: "index", method: "GET", namespace: "api-v1")
		"/api/v1/stream"(controller: "streamApi", action: "create", method: "POST", namespace: "api-v1")
	}
}