class UnifinaCorePluginUrlMappings {
	static mappings = {
		"/localFeedFile/$feedDir/$day/$file"(controller:"localFeedFile",action:"index")
	}
}