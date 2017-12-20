package pages

class StreamConfigurePage extends GrailsPage {
	static controller = "stream"
	static action = "configure"
	
	static url = "$controller/$action"

	static content = {
		navbar { module NavbarModule }
		saveButton { $("button.save") }
		autodetectButton { $("#autodetect") }
		fieldsTable { $("#stream-fields table") }
	}
}