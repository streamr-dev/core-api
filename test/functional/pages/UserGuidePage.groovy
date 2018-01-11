package pages

class UserGuidePage extends GrailsPage {
    
    static controller = "help"
    static action = "userGuide"

    static url = "$controller/$action"

    static content = {
		tableOfContents { $("nav.streamr-sidebar > ul.nav") }
	}
}

