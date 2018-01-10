package pages

class CanvasListPage extends GrailsPage {

	static controller = "canvas"
	static action = "list"
	
	static url = "$controller/$action"
	
	static content = {
		navbar { module NavbarModule }
		canvasTable { $(".table") }
		canvasTableBody { $(".table .tbody") }
	}
}
