class UnifinaCorePluginUrlMappings {
	static mappings = {
		"/localFeedFile/$feedDir/$day/$file"(controller:"localFeedFile",action:"index")
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		// Root should be mapped in app, not in plugin, because plugin url mappings can't currently be overridden
		
		"500"(view:'/error')
		
		"/login/$action?"(controller: "login")
		"/logout/$action?"(controller: "logout")
		
		"/webcomponents/index.html"(view:"/webcomponents/index")
		"/webcomponents/streamr-client.html"(view:"/webcomponents/streamr-client")
		"/webcomponents/streamr-label.html"(view:"/webcomponents/streamr-label")
		"/webcomponents/streamr-chart.html"(view:"/webcomponents/streamr-chart")
		"/webcomponents/streamr-heatmap.html"(view:"/webcomponents/streamr-heatmap")
	}
}