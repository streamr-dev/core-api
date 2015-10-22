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
			$("#main-menu .navigation .runningsignalpath", text: contains('DashboardSpec')).displayed
		}
		
		// Open a rsp
		when: "a rsp clicked to open"
		$("#main-menu .navigation .runningsignalpath", text: contains('DashboardSpec')).click()
		then: "uichannel-list opens"
		waitFor { $(".uichannel-title").displayed }
		
		// Add a module
		when: "uichannel clicked"
		$(".uichannel-title", 0).click()
		then: "one dashboarditem should be visible"
		waitFor { $("#dashboard-view .dashboarditem").displayed }
		
		// Click to edit the title of the module
		when: "clicked to edit the title"
		$("#dashboard-view .dashboarditem .titlebar-clickable").click()
		then: "title changes to input"
		waitFor { $("#dashboard-view .dashboarditem .titlebar-edit").displayed }
		
		// Edit the title of the module
		when: "dashboarditem title changed"
		$("#dashboard-view .dashboarditem .titlebar-edit").firstElement().clear()
		$("#dashboard-view .dashboarditem .titlebar-edit") << "New title"
		nameInput.click()
		then: "title changes"
		$("#dashboard-view .dashboarditem .titlebar-clickable", text:"New title").displayed
		
		when: "the dashboard name changed"
		nameInput.firstElement().clear()
		nameInput << dashboardName + "2"
		
		then: "the name changes"
		nameInput.value() == dashboardName + "2"
		
		when: "saved"
		saveButton.click()
		then: "pnotify with title 'Saved!' appears"
		waitFor { $("div.ui-pnotify .ui-pnotify-title", text:"Saved!").displayed }
		
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
		js.exec("return \$('#dashboard-view').sortable( 'option', 'disabled' )") == true
		then: "the dashboarditem should have the same title"
		$("#dashboard-view .dashboarditem .titlebar", text:"New title").displayed
		
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
		waitFor { at DashboardShowPage }
		
		when: "clicked to edit"
		$("#main-menu-toggle").click()
		then: "the dashboard should be in edit-mode"
		waitFor { js.exec("return \$('#main-menu').width()") > 0 }
		js.exec("return \$('#dashboard-view').sortable( 'option', 'disabled' )") == false
		
		
		// Delete the module
		when: "clicked delete-button"
		$("#dashboard-view .dashboarditem .delete-btn").click()
		then: "the item should be removed"
		!$("#dashboard-view .dashboarditem").displayed
		
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
