package pages

class ModuleBrowserPage extends GrailsPage {
    
    static controller = "module"
    static action = "list"

    static url = "$controller/$action"

    static content = {
		tableOfContents { $("nav.streamr-sidebar > ul.nav") }
    }
}

