package pages

class DashboardEditorPage extends GrailsPage {

	static controller = "dashboard"
	static action = "editor"
	
	static url = "$controller/$action"

	static at = {
		waitFor {
			saveButton.displayed
		}
	}
	
	static content = {
		navbar { module NavbarModule }
		sidebar { $(".sidebar_sidebar") }
		canvasTitle { sidebar.find(".canvasInList_canvasInList") }
		saveButton { $(".dashboardTools_saveButton") }
		runningCanvasesLabel { $(".canvasList_canvasListTitle label") }
		shareButton { $(".dashboardTools_shareButton") }
		menuToggle { $(".breadcrumb_streamrDropdownButton") }
		dropdownShareButton { $(".editor_dropdownShareButton") }
		dropdownDeleteButton { $(".editor_dropdownDeleteButton") }
		deleteButton { $(".dashboardTools_deleteButton") }
		dashboardNameLabel { $(".nameEditor_nameEditor label")}
		dashboardNameInput { $(".nameEditor_nameEditor input")}
	}
}

