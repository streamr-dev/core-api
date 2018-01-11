package pages

class DashboardListPage extends GrailsPage {

	static controller = "dashboard"
	static action = "list"
	
	static url = "$controller/$action"
	
	static content = {
		navbar { module NavbarModule }
		createButton { $("#createButton") }
		dashboardTable { $(".table") }
	}
}

