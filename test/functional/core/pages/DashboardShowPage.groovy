package core.pages

class DashboardShowPage extends GrailsPage {

	static controller = "dashboard"
	static action = "show"
	
	static url = "$controller/$action"
	
	static content = {
		navbar { module NavbarModule }
		mainMenu { $("#main-menu") }
		saveButton { $("#saveButton") }
		runningCanvasesLabel { $(".canvas-title label") }
		shareButton { $("#share-button") }
		dropdownShareButton { $(".share-dashboard-button") }
		deleteButton { $(".delete-dashboard-button") }
		dashboardNameLabel { $(".name-editor .name")}
		dashboardNameInput { $(".name-editor input")}
		menuToggle { $(".dashboard-menu-toggle") }
	}
}

