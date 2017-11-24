package core.mixins

trait ListPageMixin {

	def findRow(String name, boolean scroll = true) {
		if (scroll) {
			scrollToRow(name)
		}
		return $(".table .td", text: name, 0).parents(".tr")
	}

	def clickRow(String name) {
		findRow(name).click()
	}

	def findDropdownButton(String name) {
		findRow(name).find(".streamr-dropdown")
	}

	def clickDropdownButton(String name) {
		findDropdownButton(name).click()
	}

	def findDeleteButton(String name) {
		clickDropdownButton(name)
		findRow(name).find(".delete-canvas-link")
	}

	def clickDeleteButton(String name) {
		findDeleteButton(name).click()
	}

	def findShareButton(String name) {
		clickDropdownButton(name)
		findRow(name).find(".share-button")
	}

	def clickShareButton(String name) {
		findShareButton(name).click()
	}

	def scrollToRow(String name) {
		js.exec("""
			var element = jQuery(".table .td").filter(function() {
				return jQuery(this).text().trim() === "$name"
			}).eq(0)
			var siteHeaderHeightInPx = 40
			window.scrollTo(0, element.offset().top - siteHeaderHeightInPx)
		""")
		waitFor {
			findRow(name, false).displayed
		}
	}
}
