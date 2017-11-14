package core.pages

import geb.Page

class AccessDeniedPage extends Page {

	static at = {
		waitFor { 
			$("#main-navbar").displayed
			$("body", text: contains("not authorized")) 
		}
	}

}

