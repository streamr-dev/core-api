class UrlMappings {

	static mappings = {
		// Remember to change UnifinaCorePluginUrlMappings!!!
		"/localFeedFile/$feedDir/$day/$file"(controller:"localFeedFile",action:"index")
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(controller:"canvas")
		
		// 403 would be handled by Spring Security Core by default, but due to
		// https://jira.grails.org/browse/GPSPRINGSECURITYCORE-253 it needs to be specified explicitly
		"403"(controller: "login", action: "denied")
		"500"(view:'/error')
	
		"/webcomponents/$view"(controller: "webcomponents", action: "index")
		
		// API url mappings
		"/api/stream/create"(controller: "stream", action: "apiCreate")
		"/api/stream/lookup"(controller: "stream", action: "apiLookup")
	}
}
