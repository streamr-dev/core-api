package pages

class StreamCreatePage extends GrailsPage {

	static controller = "stream"
	static action = "create"

	static url = "$controller/$action"

	static content = {
		navbar { module NavbarModule }
		name { $("input", name: "name") }
		description { $("input", name: "description") }
		nextButton { $("button", name: "next") }
		feed { $("select", name: "feed") }
	}
}
