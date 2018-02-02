import LoginTester1Spec
import mixins.CanvasMixin
import mixins.ConfirmationMixin
import mixins.DashboardMixin
import mixins.ListPageMixin
import mixins.NotificationMixin
import pages.*
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class DashboardPageFunctionalSpec extends LoginTester1Spec implements CanvasMixin, ConfirmationMixin, DashboardMixin, NotificationMixin, ListPageMixin {

	@Shared
	String name
	@Shared
	String name2

	def setupSpec() {
		name = "test" + new Date().getTime()
		name2 = name + "2"

		super.login()
		waitFor { at CanvasPage }

		// Go start the Canvas related to this spec
		to CanvasListPage
		waitFor { at CanvasListPage }
		clickRow("DashboardSpec")
		waitFor { at CanvasPage }
		// Wait for the canvas to load
		waitFor { findModuleOnCanvas("Chart") }

		ensureRealtimeTabDisplayed()
		saveCanvas()
		startCanvas(true)

		noNotificationsVisible()
		$("#navSettingsLink").click()
		$("#navLogoutLink").displayed
		$("#navLogoutLink").click()
		waitFor { at LoginPage }
	}

	def cleanupSpec() {
		super.login()

		to CanvasListPage
		waitFor { at CanvasListPage }
		clickRow("DashboardSpec")

		waitFor { at CanvasPage }
		stopCanvasIfRunning()
	}

	void "dashboard can be created"() {
//		Creating a new dashboard
		to DashboardListPage
		waitFor { at DashboardListPage }

		// Create a dashboard
		when: "clicked to create new dashboard"
		createButton.click()
		then: "go to the dashboard show page"
		waitFor { at DashboardEditorPage }

		when: "changed the name"
		setDashboardName(name)
		then: "saved successfully"
		saveDashboard()
	}

	void "dashboard can be edited"() {
		setup: "open the dashboard"
		to DashboardListPage
		clickRow(name)
		waitFor {
			at DashboardEditorPage
		}

		// Open canvas
		when: "canvas clicked to open"
		findCanvas("DashboardSpec").click()
		then: "module list opens"
		waitFor { findCanvas("DashboardSpec").find(".moduleInModuleList_module")[0].displayed }

		// Add some modules
		when: "Label added"
		findModule("DashboardSpec", "Label").click()
		then: "Label should be displayed"
		waitFor { findDashboardItem("Label").displayed }

		when: "Table added"
		findModule("DashboardSpec", "Table").click()
		then: "Table item should be displayed"
		waitFor { findDashboardItem("Table").displayed }

		// Click to edit the title of the module
		when: "clicked to edit the title"
		findDashboardItem("Label").find(".dashboardItemTitleRow_startEditButton").click()
		then: "title changes to input"
		waitFor { findTitleInput("Label").displayed }

		// Edit the title of the module
		when: "dashboarditem title changed"
		findTitleInput("Label") << "2"
		runningCanvasesLabel.click()

		then: "title changes"
		waitFor { findDashboardItem("Label2").displayed }

		expect: "the dashboard name changed"
		setDashboardName(name2)

		// Testing of dragging the items
		when: "dragged the item"
		dragDashboardItem("Label2", 300, 0)
		then: "hasn't failed"
		at DashboardEditorPage
		saveDashboard()

		//Checking the modifications have been saved
		when: "went to the dashboard list page"
		to DashboardListPage
		then: "the new dashboard is visible"
		waitFor { at DashboardListPage }
		findRow(name2)

		when: "clicked the new dashboard to open"
		clickRow(name2)
		then: "the dashboard should open in non-edit-mode"
		waitFor { at DashboardEditorPage }
		then: "the dashboarditem should have the same title"
		findDashboardItem("Label2").displayed
	}

	void "dashboard can be deleted"() {
		setup: "open the dashboard"
		to DashboardListPage
		clickRow(name2)
		waitFor {
			at DashboardEditorPage
		}

		// Click to delete the dashboard without accepting it
		when: "clicked the delete-button"
		deleteButton.click()
		then: "confirmation should appear"
		waitFor { $(".modal-content .modal-body", text:contains("remove")).displayed }

		when: "clicked 'cancel'"
		$(".modal-footer button", text:"Cancel").click()
		then: "back to show-page"
		waitFor { at DashboardEditorPage }
		then: "confirmation is not visible anymore"
		waitFor { !($(".modal-content .modal-body", text:contains("remove")).displayed) }

		when: "went to the dashboard list page"
		to DashboardListPage
		then: "the new dashboard is visible"
		waitFor { at DashboardListPage }
		findRow(name2)

		when: "clicked the new dashboard to open"
		clickRow(name2)
		then: "the dashboard should open in non-edit-mode"
		waitFor {
			at DashboardEditorPage
		}
		// Delete the dashboard items
		when: "all dashboarditems are deleted"
		findDashboardItem("Label2").find(".dashboardItemTitleRow_deleteButton").click()
		findDashboardItem("Table").find(".dashboardItemTitleRow_deleteButton").click()
		then: "the item should be removed"
		waitFor { $(".dashboardItem_dashboardItem").size() == 0 }

		// Delete the dashboard
		when: "clicked the delete button"
		deleteButton.click()
		then: "confirmation should appear"
		waitFor { $(".modal-content .modal-body", text: contains("remove")).displayed }

		when: "clicked 'Ok'"
		$(".modal-footer button", text:"OK").click()
		then: "go to list-page"
		waitFor {
			at DashboardListPage
		}

		then: "the old dashboard is not in the list anymore"
		!findRow(name2, false)
	}

	void "non-existing dashboard cannot be opened"() {
		when: "try to upload a non-existing dashboard"
		go "dashboard/editor/asdfasfasdfsadfasfar"
		then: "error shown"
		waitForErrorNotification()
	}
}