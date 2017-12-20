package mixins


import pages.DashboardListPage
import pages.DashboardShowPage
import org.openqa.selenium.Keys


class DashboardMixin {

	def findCanvas(String name) {
		return $("#main-menu .navigation .canvas .mm-text", text: name).closest(".canvas")
	}

	def expandCanvas(String name) {
		def canvas = findCanvas(name)
		if (!canvas.hasClass("open")) {
			canvas.click()
		}
	}

	def findModule(String canvasName, String moduleName) {
		return findCanvas(canvasName)?.find(".module-title", text:contains(moduleName))
	}

	def findDashboardItem(String name) {
		return $("#dashboard-view .dashboarditem .title", text:contains(name)).parents(".dashboarditem")
	}

	def findTitleInput(String title) {
		return $("#dashboard-view .dashboarditem input", value: title)
	}

	def createDashboard(String name) {
		to DashboardListPage
		createButton.click()
		waitFor { at DashboardShowPage }
		setDashboardName(name)
		saveDashboard()
	}

	def deleteDashboard(String dashboardName) {
		to DashboardListPage
		waitFor { at DashboardListPage }
		$(".table .td", text: dashboardName).click()
		waitFor {
			at DashboardShowPage
		}
		if ($("body.editing").size() == 0) {
			$("#main-menu-toggle").click()
			waitFor {
				saveButton.displayed
			}
		}
		menuToggle.click()
		waitFor {
			deleteButton.displayed
		}
		deleteButton.click()
		waitForConfirmation()
		acceptConfirmation()
		waitFor { at DashboardListPage }
		$(".alert", text:contains("Dashboard " +dashboardName+ "deleted")).displayed
		!($(".table .td", text:dashboardName).displayed)
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
		dashboardNameInput << name
		dashboardNameInput << Keys.ENTER
		waitFor { dashboardNameLabel.displayed }
	}

	def saveDashboard() {
		saveButton.click()
		waitFor { $(".ui-pnotify .ui-pnotify-title", text:"Saved!").displayed }
	}

	void dragDashboardItem(name, x, y) {
		def item = findDashboardItem(name).find(".title")

		interact {
			clickAndHold(item)
			moveByOffset(x, y)
			release()
		}
	}
}
