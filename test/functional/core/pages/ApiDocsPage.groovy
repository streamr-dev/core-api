package core.pages

class ApiDocsPage extends GrailsPage {
    
    static controller = "help"
    static action = "api"

    static url = "$controller/$action"

	static content = {
		tableOfContents { $("nav.streamr-sidebar > ul.nav") }
	}
}

