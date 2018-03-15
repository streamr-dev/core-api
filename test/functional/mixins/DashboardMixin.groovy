package mixins

import pages.DashboardListPage
import pages.DashboardEditorPage
import org.openqa.selenium.Keys

trait DashboardMixin {

	def findCanvas(String name) {
		return $(".canvasInList_canvasTitle", text: name).parent().parent()
	}

	def expandCanvas(String name) {
		def canvas = findCanvas(name)
		if (!canvas.hasClass("open")) {
			canvas.click()
		}
	}

	def findModule(String canvasName, String moduleName) {
		return findCanvas(canvasName)?.find(".moduleInModuleList_module", text:contains(moduleName))
	}

	def findDashboardItem(String name) {
		return $("#content-wrapper .dashboardItem_dashboardItem .dashboardItemTitleRow_title", text:contains(name)).parents(".dashboardItem_dashboardItem")
	}

	def findTitleInput(String title) {
		return $("#content-wrapper .dashboardItem_dashboardItem input", value: title)
	}

	def createDashboard(String name) {
		to DashboardListPage
		createButton.click()
		waitFor { at DashboardEditorPage }
		setDashboardName(name)
		saveDashboard()
	}

	def deleteDashboard(String dashboardName) {
		to DashboardListPage
		waitFor { at DashboardListPage }
		$(".table .td", text: dashboardName).click()
		waitFor {
			at DashboardEditorPage
		}
		deleteButton.click()
		waitForConfirmation()
		acceptConfirmation()
		waitFor { at DashboardListPage }
	}

	def addDashboardItem(String canvasName, String dashboardItemName) {
		expandCanvas(canvasName)
		waitFor {
			findModule(canvasName, dashboardItemName).displayed
		}
		findModule(canvasName, dashboardItemName).click()
		waitFor {
			findDashboardItem(dashboardItemName).displayed
		}
	}

	def setDashboardName(String name) {
		dashboardNameLabel.click()
		waitFor { dashboardNameInput.displayed }
		dashboardNameInput.firstElement().clear()
		dashboardNameInput << name
		dashboardNameInput << Keys.ENTER
		waitFor { dashboardNameLabel.displayed }
	}

	def clickDropdownShareButton() {
		menuToggle.click()
		waitFor {
			dropdownShareButton.displayed
		}
		dropdownShareButton.click()
	}

	def saveDashboard() {
		interact {
			moveToElement(saveButton)
		}
		saveButton.click()
		waitFor { findSuccessNotification() }
	}

	void dragDashboardItem(name, x, y) {
		def item = findDashboardItem(name).find(".dashboardItem_header")

		interact {
			clickAndHold(item)
			moveByOffset(x, y)
			release()
		}
	}
}
