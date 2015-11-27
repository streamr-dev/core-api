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
		
		"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")
		"/user/$action?/$id?"(controller: "user", namespace:"streamr")
		"/register/$action?/$id?"(controller: "register", namespace:"streamr")
		
		"/webcomponents/$view"(controller: "webcomponents", action: "index")
		
		// API url mappings
		"/api/stream/create"(controller: "stream", action: "apiCreate")
		"/api/stream/lookup"(controller: "stream", action: "apiLookup")
		"/api/live/request"(controller: "live", action: "request")
	}
}