import pages.*
import spock.lang.*
import core.LoginTester1Spec;
import core.mixins.CanvasMixin
import core.pages.DashboardCreatePage
import core.pages.DashboardListPage
import core.pages.DashboardShowPage
import core.pages.LoginPage

@Mixin(CanvasMixin)
class DashboardSpec extends LoginTester1Spec {

	def findRunningSignalPath(String name) {
		return $("#main-menu .navigation .runningsignalpath", text: contains(name))
	}
	
	def findDashboardItem(String name) {
		return $("#dashboard-view .dashboarditem .title", text:contains(name)).parents(".dashboarditem")
	}
	
	def findTitleInput(String title) {
		return $("#dashboard-view .dashboarditem input", value: title)
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
				findRunningSignalPath("DashboardSpec").displayed
			}
		
		// Open a rsp
		when: "a rsp clicked to open"
			findRunningSignalPath("DashboardSpec").click()
		then: "uichannel-list opens"
			waitFor { findRunningSignalPath("DashboardSpec").find(".uichannel-title").displayed }
		
		// Add some modules
		when: "Label added"
			findRunningSignalPath("DashboardSpec").find(".uichannel-title", text:contains("Label")).click()
		then: "Label should be visible"
			waitFor { findDashboardItem("Label").displayed }
		
		when: "Table added"
			findRunningSignalPath("DashboardSpec").find(".uichannel-title", text:contains("Table")).click()
		then: "Table should be visible and pnotify displayed"
			waitFor { 
				findDashboardItem("Table").displayed
				$(".ui-pnotify .alert").displayed 
			}
		
		when: "Chart added"
			findRunningSignalPath("DashboardSpec").find(".uichannel-title", text:contains("Chart")).click()
		then: "Chart should be visible and pnotify displayed"
			waitFor {
				findDashboardItem("Chart").displayed
				$(".ui-pnotify .alert").displayed
			}
		
		// Click to edit the title of the module
		when: "clicked to edit the title"
			findDashboardItem("Label").find(".titlebar-clickable").click()
		then: "title changes to input"
			waitFor { findTitleInput("Label").displayed }
		
		// Edit the title of the module
		when: "dashboarditem title changed"
			findTitleInput("Label").firstElement().clear()
			findTitleInput("") << "New title"
			// Focus lost
			nameInput.click()
		then: "title changes"
			waitFor { findDashboardItem("New title").displayed }
		
		when: "the dashboard name changed"
			nameInput.firstElement().clear()
			nameInput << dashboardName + "2"
		
		then: "the name changes"
			nameInput.value() == dashboardName + "2"
		
		when: "saved"
			saveButton.click()
		then: "pnotify with title 'Saved!' appears"
			waitFor { $(".ui-pnotify .ui-pnotify-title", text:"Saved!").displayed }
		
		//Checking the modifications have been saved	
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
			findDashboardItem("New title").displayed
		
		when: "clicked to edit"
			$("#main-menu-toggle").click()
		then: "the dashboard should be in edit-mode"
			waitFor { js.exec("return \$('#main-menu').width()") > 0 }
			js.exec("return \$('#dashboard-view').sortable( 'option', 'disabled' )") == false
		
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
				js.exec("return \$('#main-menu').width()") == 0
			}
			// Hide pnotifys so that they don't obstruct the close buttons
			waitFor { $(".ui-pnotify").size()==2 }
			waitFor { $(".ui-pnotify").size()==0 }
		
		when: "clicked to edit"
			$("#main-menu-toggle").click()
		then: "the dashboard should be in edit-mode"
			waitFor { 
				js.exec("return \$('#main-menu').width()") > 0
				js.exec("return \$('#dashboard-view').sortable( 'option', 'disabled' )") == false
			}
			
		// Delete the dashboard items
		when: "all dashboarditems are deleted"
			findDashboardItem("New title").find(".delete-btn").click()
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
