import core.LoginTester1Spec
import core.mixins.ListPageMixin
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.mixins.DashboardMixin
import core.pages.*
import pages.*

class InputModuleDashboardSpec extends LoginTester1Spec {

	static String canvasTemplate = "InputModuleDashboardSpec"
	static String canvasName = canvasTemplate + System.currentTimeMillis()
	static String dashboardName = "InputModuleDashboardSpec" + System.currentTimeMillis()

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(ListPageMixin)
		this.class.metaClass.mixin(ConfirmationMixin)
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
			!$(".ui-pnotify").displayed
		}

		navbar.navSettingsLink.click()
		navbar.navLogoutLink.click()
	}

	def setup() {
		openDashboard()
	}

	def openDashboard() {
		to DashboardListPage
		$(".clickable-table a span", text: dashboardName).click()
		waitFor {
			at DashboardShowPage
		}
		if ($("body.editing").size() == 0) {
			$("#main-menu-toggle").click()
			waitFor {
				saveButton.displayed
			}
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
		button = findDashboardItem("Button").find("button.button-module-button")
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
		sendBtn = findDashboardItem("TextField").find(".btn.send-btn")
		then: "The text in the textField is textFieldTest"
		// Geb's own .text() didn't work for some reason
		waitFor {
			js.exec("return \$('streamr-text-field textarea').val()") == "textFieldTest"
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
		switcher = findDashboardItem("Switcher").find("div.switcher div.switcher-inner")

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
			$(".switcher.checked").size() == 1
		}
	}
}
