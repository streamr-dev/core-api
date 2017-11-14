package core.pages

class DashboardShowPage extends GrailsPage {

	static controller = "dashboard"
	static action = "show"
	
	static url = "$controller/$action"
	
	static content = {
		navbar { module NavbarModule }
		mainMenu { $("#main-menu") }
		nameInput { $("#main-menu .dashboard-name")}
		saveButton { $("#main-menu .save-button") }
		shareButton { $("#share-button") }
		deleteButton { $("#deleteDashboardButton") }
	}
}

