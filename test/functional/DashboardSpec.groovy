import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.mixins.DashboardMixin
import core.pages.*

class DashboardSpec extends LoginTester1Spec {

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(CanvasMixin)
		this.class.metaClass.mixin(ConfirmationMixin)
		this.class.metaClass.mixin(DashboardMixin)

		super.login()
		waitFor { at CanvasPage }
		
		// Go start the Canvas related to this spec
		to CanvasListPage
		waitFor { at CanvasListPage }
		$(".table .td", text:"DashboardSpec").click()
		waitFor { at CanvasPage }
		// Wait for the canvas to load
		waitFor { findModuleOnCanvas("Chart") }

		ensureRealtimeTabDisplayed()
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
		$(".table .td", text:"DashboardSpec").click()

		waitFor { at CanvasPage }
		stopCanvasIfRunning()
	}
	
	void "the flow of creating, modifying and deleting a dashboard works correctly"() {
//		Creating a new dashboard
		to DashboardListPage
		waitFor { at DashboardListPage }
		
		String dashboardName = "test" + new Date().getTime()
		
		// Create a dashboard
		when: "clicked to create new dashboard"
			createButton.click()
		then: "go to the dashboard create page"
			waitFor { at DashboardCreatePage }
		
		when: "created a new dashboard"
			nameInput << dashboardName
			createButton.click()
		then: "go to dashboard show page"
			waitFor { 
				at DashboardShowPage
				findCanvas("DashboardSpec").displayed
			}
		
		// Open a rsp
		when: "a rsp clicked to open"
			findCanvas("DashboardSpec").click()
		then: "module list opens"
			waitFor { findCanvas("DashboardSpec").find(".module-title").displayed }
		
		// Add some modules
		when: "Label added"
			findModule("DashboardSpec", "Label").click()
		then: "Label should be displayed"
			waitFor { findDashboardItem("Label").displayed }
		
		when: "Table added"
			findModule("DashboardSpec", "Table").click()
		then: "Table item should be displayed"
			waitFor { findDashboardItem("Table").displayed }
		
		when: "Chart added"
			findModule("DashboardSpec", "Chart").click()
		then: "Chart item should be displayed"
			waitFor { findDashboardItem("Chart").displayed }
		
		// Click to edit the title of the module
		when: "clicked to edit the title"
			findDashboardItem("Label").find(".titlebar-clickable").click()
		then: "title changes to input"
			waitFor { findTitleInput("Label (Stream.temperature)").displayed }
		
		// Edit the title of the module
		when: "dashboarditem title changed"
			findTitleInput("Label (Stream.temperature)").firstElement().clear()
			findTitleInput("") << "Foo"
			// Focus lost
			nameInput.click()
		then: "title changes"
			waitFor { findDashboardItem("Foo").displayed }
		
		when: "the dashboard name changed"
			nameInput.firstElement().clear()
			nameInput << dashboardName + "2"
		
		then: "the name changes"
			nameInput.value() == dashboardName + "2"
		
		when: "saved"
			saveButton.click()
		then: "pnotify with title 'Saved!' appears"
			waitFor { $(".ui-pnotify .ui-pnotify-title", text:"Saved!").displayed }
		
		// Checking the modifications have been saved
		when: "went to the dashboard list page"
			to DashboardListPage
		then: "the new dashboard is visible"
			waitFor { at DashboardListPage }
			$(".table .td", text:dashboardName + "2").displayed
		
		when: "clicked the new dashboard to open"
			$(".table .td", text:dashboardName + "2").click()
		then: "the dashboard should open in non-edit-mode"
			waitFor { at DashboardShowPage }
			waitFor { js.exec("return \$('#main-menu').width()") == 0 }
			waitFor { js.exec("return \$('#dashboard-view').sortable( 'option', 'disabled' )") == true }
		then: "the dashboarditem should have the same title"
			findDashboardItem("Foo").displayed
		
		when: "clicked to edit"
			$("#main-menu-toggle").click()
		then: "the dashboard should be in edit-mode"
			waitFor { js.exec("return \$('#main-menu').width()") > 0 }
			js.exec("return \$('#dashboard-view').sortable( 'option', 'disabled' )") == false

		// Testing of dragging the items
		when: "dragged the item everywhere"
			dragDashboardItem("Table", 0, -300)
			dragDashboardItem("Table", -300, -300)
			dragDashboardItem("Table", -300, 0)
		then: "hasn't failed"
			at DashboardShowPage

		// Click to delete the dashboard without accepting it
		when: "clicked the delete-button"
			deleteButton.click()
		then: "confirmation should appear"
			waitFor { $(".modal-content .bootbox-body", text:"Really delete dashboard " +dashboardName+ "2?").displayed }
		
		when: "clicked 'cancel'"
			$(".modal-footer button", text:"Cancel").click()
		then: "back to show-page"
			waitFor { at DashboardShowPage }
			then: "confirmation is not visible anymore"
			waitFor { !($(".modal-content .bootbox-body", text:"Really delete dashboard " +dashboardName+ "2?").displayed) }
		
		when: "went to the dashboard list page"
			to DashboardListPage
		then: "the new dashboard is visible"
			waitFor { at DashboardListPage }
			$(".table .td", text:dashboardName + "2").displayed
		
		when: "clicked the new dashboard to open"
			$(".table .td", text:dashboardName + "2").parent().click()
		then: "the dashboard should open in non-edit-mode"
			waitFor { 
				at DashboardShowPage
			}
		
		when: "clicked to edit"
			$("#main-menu-toggle").click()
		then: "the dashboard should be in edit-mode"
			waitFor { 
				findCanvas("DashboardSpec").displayed
				js.exec("return \$('#dashboard-view').sortable( 'option', 'disabled' )") == false
			}
			
		// Delete the dashboard items
		when: "all dashboarditems are deleted"
			findDashboardItem("Foo").find(".delete-btn").click()
			findDashboardItem("Table").find(".delete-btn").click()
			findDashboardItem("Chart").find(".delete-btn").click()
		then: "the item should be removed"
			waitFor { $(".dashboarditem").size()==0 }
		
		// Delete the dashboard
		when: "clicked the delete button"
			deleteButton.click()
		then: "confirmation should appear"
			waitFor { $(".modal-content .bootbox-body", text:"Really delete dashboard " +dashboardName+ "2?").displayed }
		
		when: "clicked 'Ok'"
			$(".modal-footer button", text:"OK").click()
		then: "go to list-page"
			waitFor { at DashboardListPage }
		then: "alert 'Deleted' is shown"
			$(".alert", text:contains("Dashboard " +dashboardName+ "2 deleted")).displayed
		then: "the old dashboard is not in the list anymore"
			!($(".table .td", text:dashboardName + "2").displayed)		
	}
}
