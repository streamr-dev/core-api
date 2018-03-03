package mixins

trait ListPageMixin {

	def findRow(String name, boolean scroll = true) {
		def row = $(".table .td", text: name, 0).parents(".tr")
		if (scroll) {
			interact {
				moveToElement(row)
			}
			waitFor {
				row.displayed
			}
		}
		return row
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
}
