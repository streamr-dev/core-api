package core.pages

class ConfigureMongoPage extends GrailsPage {
	static controller = "stream"
	static action = "configureMongo"

	static url = "$controller/$action"

	static content = {
		host { $("input", name: "host") }
		port { $("input", name: "port") }
		username { $("input", name: "username") }
		password { $("input", name: "password") }
		database { $("input", name: "database") }
		collection { $("input", name: "collection") }
		timestampKey { $("input", name: "timestampKey") }
		timestampType { $("select", name: "timestampType") }
		pollIntervalMillis { $("input", name: "pollIntervalMillis") }
		query { $("textarea", name: "query") }
		submit { $("input", name: "submit") }
	}
}
