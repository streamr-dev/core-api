import core.LoginTester1Spec
import core.mixins.ConfirmationMixin
import core.mixins.DashboardMixin
import core.pages.*
import pages.*

class InputModuleDashboardSpec extends LoginTester1Spec {

	static String liveCanvasName = "InputModuleDashboardSpec"
	static String dashboardName = "test" + new Date().getTime()

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(ConfirmationMixin)
		this.class.metaClass.mixin(DashboardMixin)

		super.login()
		waitFor { at CanvasPage }

		// Go start the RunningSignalPath related to this spec
		to LiveListPage
		waitFor { at LiveListPage }
		$(".table .td", text: liveCanvasName).click()
		waitFor { at LiveShowPage }
		if (stopButton.displayed) {
			stopButton.click()
			waitForConfirmation()
			acceptConfirmation()
			waitFor { startButton.displayed }
		}

		startButton.click()
		waitFor { stopButton.displayed }

		createDashboard(dashboardName)

		navbar.navSettingsLink.click()
		navbar.navLogoutLink.click()
	}

	def setup() {
		to DashboardListPage
		$(".table .td", text: dashboardName).click()
		waitFor {
			at DashboardShowPage
		}
		addDashboardItem(liveCanvasName, "Table")
		saveButton.click()
	}

	def cleanupSpec() {
		// Delete the dashboard
		super.login()
		waitFor { at CanvasPage }
		deleteDashboard(dashboardName)
		save()
	}

	void "the buttonModule works"() {
		def button
		when: "Button added"
		addModule(liveCanvasName, "Button")
		button = findDashboardItem("Button").find("button.button-module-button")
		then: "The name of the button is buttonTest"
		button.text() == "buttonTest"

		when: "Button clicked"
		button.click()
		then: "The table gets the message"
		$("streamr-table table.event-table-module-content tbody tr").size() == 1
	}
}
