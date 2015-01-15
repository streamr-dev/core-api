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
		"500"(view:'/error')
		
		"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")
	}
}
