class UrlMappings {

	static mappings = {
		// Remember to change UnifinaCorePluginUrlMappings!!!
		"/localFeedFile/$feedDir/$day/$file"(controller:"localFeedFile",action:"index")
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
