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
	
		"/webcomponents/index.html"(view:"/webcomponents/index.gsp")
		"/webcomponents/streamr-client.html"(view:"/webcomponents/streamr-client.gsp")
		"/webcomponents/streamr-label.html"(view:"/webcomponents/streamr-label.gsp")
		"/webcomponents/streamr-chart.html"(view:"/webcomponents/streamr-chart.gsp")
	}
}
