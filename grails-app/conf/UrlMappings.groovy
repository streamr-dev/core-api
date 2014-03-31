class UrlMappings {

	static mappings = {
		"/feedFile/$feedDir/$day/$file"(controller:"feedFile",action:"index")
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
