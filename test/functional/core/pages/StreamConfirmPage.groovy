package core.pages

import geb.Page
class StreamConfirmPage extends GrailsPage {
	static controller = "stream"
	static action = "confirm"
	
	static url = "$controller/$action"

	static content = {
		navbar { module NavbarModule }
		
		
	}
}