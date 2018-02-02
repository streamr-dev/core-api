import LoginTester1Spec
import mixins.*
import pages.*

class InputModuleDashboardSpec extends LoginTester1Spec implements CanvasMixin, ConfirmationMixin, DashboardMixin, ListPageMixin, NotificationMixin {

	static String canvasTemplate = "InputModuleDashboardSpec"
	static String specCanvasName = canvasTemplate + System.currentTimeMillis()
	static String dashboardSpecName = "InputModuleDashboardSpec" + System.currentTimeMillis()

	def setupSpec() {
		super.login()
		waitFor { at CanvasPage }

		// Go start the RunningSignalPath related to this spec
		to CanvasListPage
		waitFor { at CanvasListPage }
		clickRow(canvasTemplate)
		waitFor { at CanvasPage }

		// Create a copy of the canvas unique for this test
		saveCanvasAs(specCanvasName)

		ensureRealtimeTabDisplayed()
		resetAndStartCanvas(true)

		createDashboard(dashboardSpecName)

		addDashboardItem(specCanvasName, "Table")

		saveDashboard()

		waitFor {
			!($(".notifications-wrapper .notification").displayed)
		}

		navbar.navSettingsLink.click()
		navbar.navLogoutLink.click()
	}

	def setup() {
		openDashboard()
	}

	def openDashboard() {
		to DashboardListPage
		clickRow(dashboardSpecName)
		waitFor {
			at DashboardEditorPage
		}
	}


	def cleanupSpec() {
		// Delete the dashboard
		super.login()
		deleteDashboard(dashboardSpecName)

		// Stop the canvas
		to CanvasListPage
		waitFor { at CanvasListPage }
		clickRow(specCanvasName)
		waitFor { at CanvasPage }
		stopCanvasIfRunning()

		// Delete the canvas
		to CanvasListPage
		waitFor { at CanvasListPage }
		clickDeleteButton(specCanvasName)
		waitForConfirmation()
		acceptConfirmation()
	}

	def cleanup() {
		saveDashboard()
	}

	void "the button works"() {
		def button
		when: "Button added"
		addDashboardItem(specCanvasName, "Button")
		button = findDashboardItem("Button").find(".streamr-button .btn")
		then: "The name of the button is buttonTest"
		button.text() == "buttonTest"

		when: "Button clicked"
		button.click()
		then: "The table gets the message"
		waitFor {
			$("table.event-table-module-content tbody tr").size() == 1
			$("table.event-table-module-content tbody tr td", text: "10.0").displayed
		}
	}

	void "the textField works"() {
		def textField
		def sendBtn
		when: "TextField added"
		addDashboardItem(specCanvasName, "TextField")
		textField = findDashboardItem("TextField").find("textarea")
		sendBtn = findDashboardItem("TextField").find(".streamrTextField_buttonContainer .btn")
		then: "The text in the textField is textFieldTest"
		// Geb's own .text() didn't work for some reason
		waitFor {
			js.exec("return document.querySelector('.streamrTextField_streamrTextField textarea').value") == "textFieldTest"
		}

		when: "Text changed and sendButton clicked"
		textField << "2"
		sendBtn.click()
		then: "The table gets the message"
		waitFor {
			$("table.event-table-module-content tbody tr td", text: "textFieldTest2").displayed
		}
	}

	void "the switcher works"() {
		def switcher
		addDashboardItem(specCanvasName, "Switcher")
		switcher = findDashboardItem("Switcher").find(".streamrSwitcher_switcherInner")

		when: "Switcher clicked"
		switcher.click()
		then: "The table gets the message"
		waitFor {
			$("table.event-table-module-content tbody tr td", text: "true").displayed
		}

		when: "Logged out and reloaded"
		saveDashboard()
		waitFor {
			!$(".notifications-wrapper .notification").displayed
		}

		navbar.navSettingsLink.click()
		navbar.navLogoutLink.click()
		super.login()
		openDashboard()

		then: "Switcher remembers its state"
		waitFor {
			$(".streamrSwitcher_switcher.streamrSwitcher_on").size() == 1
		}
	}
}
