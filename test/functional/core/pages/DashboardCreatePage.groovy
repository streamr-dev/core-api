package core.pages

class DashboardCreatePage extends GrailsPage {

	static controller = "dashboard"
	static action = "create"
	
	static url = "$controller/$action"
	
	static content = {
		navbar { module NavbarModule }
		nameInput { $("form").find("input") }
		createButton { $("form").find("button") }
	}
}

