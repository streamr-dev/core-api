import LoginTester1Spec
import mixins.ListPageMixin
import mixins.CanvasMixin
import mixins.ConfirmationMixin
import mixins.DashboardMixin
import mixins.NotificationMixin
import pages.*

class InputModuleDashboardSpec extends LoginTester1Spec {

	static String canvasTemplate = "InputModuleDashboardSpec"
	static String canvasName = canvasTemplate + System.currentTimeMillis()
	static String dashboardName = "InputModuleDashboardSpec" + System.currentTimeMillis()

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(ListPageMixin)
		this.class.metaClass.mixin(ConfirmationMixin)
		this.class.metaClass.mixin(NotificationMixin)
		this.class.metaClass.mixin(DashboardMixin)
		this.class.metaClass.mixin(CanvasMixin)

		super.login()
		waitFor { at CanvasPage }

		// Go start the RunningSignalPath related to this spec
		to CanvasListPage
		waitFor { at CanvasListPage }
		clickRow(canvasTemplate)
		waitFor { at CanvasPage }

		// Create a copy of the canvas unique for this test
		saveCanvasAs(canvasName)

		ensureRealtimeTabDisplayed()
		resetAndStartCanvas(true)

		createDashboard(dashboardName)

		addDashboardItem(canvasName, "Table")

		saveDashboard()

		waitFor {
			!($(".ui-pnotify").displayed)
		}

		navbar.navSettingsLink.click()
		navbar.navLogoutLink.click()
	}

	def setup() {
		openDashboard()
	}

	def openDashboard() {
		to DashboardListPage
		clickRow(dashboardName)
		waitFor {
			at DashboardEditorPage
		}
	}


	def cleanupSpec() {
		// Delete the dashboard
		super.login()
		deleteDashboard(dashboardName)

		// Stop the canvas
		to CanvasListPage
		waitFor { at CanvasListPage }
		clickRow(canvasName)
		waitFor { at CanvasPage }
		stopCanvasIfRunning()

		// Delete the canvas
		to CanvasListPage
		waitFor { at CanvasListPage }
		clickDeleteButton(canvasName)
		waitForConfirmation()
		acceptConfirmation()
	}

	def cleanup() {
		saveDashboard()
	}

	void "the button works"() {
		def button
		when: "Button added"
		addDashboardItem(canvasName, "Button")
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
		addDashboardItem(canvasName, "TextField")
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
		addDashboardItem(canvasName, "Switcher")
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
			!$(".ui-pnotify").displayed
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
