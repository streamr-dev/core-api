import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.mixins.DashboardMixin
import core.mixins.ListPageMixin
import core.mixins.NotificationMixin
import core.pages.*
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class DashboardSpec extends LoginTester1Spec {

	@Shared
	String name
	@Shared
	String name2

	void clickDeleteButton() {
		menuToggle.click()
		waitFor {
			deleteButton.displayed
		}
		deleteButton.click()
	}

	def setupSpec() {
		name = "test" + new Date().getTime()
		name2 = name + "2"
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(CanvasMixin)
		this.class.metaClass.mixin(ConfirmationMixin)
		this.class.metaClass.mixin(DashboardMixin)
		this.class.metaClass.mixin(NotificationMixin)
		this.class.metaClass.mixin(ListPageMixin)

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
		waitFor { at DashboardShowPage }

		when: "changed the name and saved"
		setDashboardName(name)
		saveButton.click()
		then: "go to dashboard show page"
		waitFor {
			findSuccessNotification()
		}
	}

	void "dashboard can be edited"() {
		setup: "open the dashboard"
			to DashboardListPage
			clickRow(name)
			waitFor {
				at DashboardShowPage
			}

		// Open canvas
		when: "canvas clicked to open"
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
			runningCanvasesLabel.click()
		then: "title changes"
			waitFor { findDashboardItem("Foo").displayed }

		expect: "the dashboard name changed"
			setDashboardName(name2)

		// Testing of dragging the items
		when: "dragged the item everywhere"
			dragDashboardItem("Table", 0, -300)
			dragDashboardItem("Table", -300, -300)
			dragDashboardItem("Table", -300, 0)
		then: "hasn't failed"
			at DashboardShowPage
			waitFor {
				// Testing that all the subscriptions are still subscribed
				browser.driver.executeAsyncScript("""
					var allSubscribed = true
					var ready = false
					var originalArguments = arguments
					\$("#client")[0].getClient(function(client) {
						\$.each(client.subById, function(k ,v) {
							if (!v.isSubscribed()) {
								allSubscribed = false
							}
						})
						originalArguments[originalArguments.length - 1](allSubscribed)
					})
				""")
			}

		when: "saved"
			saveButton.click()
		then: "pnotify with title 'Saved!' appears"
			waitFor { $(".ui-pnotify .ui-pnotify-title", text: "Saved!").displayed }

		//Checking the modifications have been saved	
		when: "went to the dashboard list page"
			to DashboardListPage
		then: "the new dashboard is visible"
			waitFor { at DashboardListPage }
			findRow(name2)

		when: "clicked the new dashboard to open"
			clickRow(name2)
		then: "the dashboard should open in non-edit-mode"
			waitFor { at DashboardShowPage }
			waitFor { js.exec("return \$('#main-menu').width()") == 0 }
			waitFor { js.exec("return \$('#dashboard-view').sortable( 'option', 'disabled' )") == true }
		then: "the dashboarditem should have the same title"
			findDashboardItem("Foo").displayed
	}

	void "dashboard can be deleted"() {
		setup: "open the dashboard"
			to DashboardListPage
			clickRow(name2)
			waitFor {
				at DashboardShowPage
			}

		when: "clicked to edit"
			$("#main-menu-toggle").click()
		then: "the dashboard should be in edit-mode"
			waitFor { js.exec("return \$('#main-menu').width()") > 0 }
			js.exec("return \$('#dashboard-view').sortable( 'option', 'disabled' )") == false

		// Click to delete the dashboard without accepting it
		when: "clicked the delete-button"
			clickDeleteButton()
		then: "confirmation should appear"
			waitFor { $(".modal-content .bootbox-body", text:contains("delete")).displayed }
		
		when: "clicked 'cancel'"
			$(".modal-footer button", text:"Cancel").click()
		then: "back to show-page"
			waitFor { at DashboardShowPage }
			then: "confirmation is not visible anymore"
			waitFor { !($(".modal-content .bootbox-body", text:contains("delete")).displayed) }
		
		when: "went to the dashboard list page"
			to DashboardListPage
		then: "the new dashboard is visible"
			waitFor { at DashboardListPage }
			findRow(name2)
		
		when: "clicked the new dashboard to open"
			clickRow(name2)
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
			clickDeleteButton()
		then: "confirmation should appear"
			waitFor { $(".modal-content .bootbox-body", text: contains("delete")).displayed }
		
		when: "clicked 'Ok'"
			$(".modal-footer button", text:"OK").click()
		then: "pnotify 'Deleted' is shown"
			waitFor {
				$(".ui-pnotify-text", text: contains("delete")).displayed
			}
		then: "go to list-page"
			waitFor {
				at DashboardListPage
			}

		then: "the old dashboard is not in the list anymore"
			!findRow(name2, false)
	}

	def "not shared dashboard cannot be opened"() {
		when: "try to upload an existing dashboard without permission"
			go "dashboard/show/456456"
		then: "error shown"
			waitForErrorNotification()
	}

	def "non-existing dashboard cannot be opened"() {
		when: "try to upload a non-existing dashboard"
			go "dashboard/show/asdfasfasdfsadfasfar"
		then: "error shown"
			waitForErrorNotification()
	}

	def "a dashboard with no share-permission doesn't show share button enabled"() {
		when: "try to upload a non-existing dashboard"
		go "dashboard/show/567567"
		then: "share button is disabled"
		waitFor {
			at DashboardShowPage
		}
		shareButton.displayed
		shareButton.getAttribute("disabled")
		dropdownShareButton.hasClass("disabled")
	}

	def "a dashboard with no write-permission doesn't show save and delete buttons enabled"() {
		when: "try to upload a non-existing dashboard"
		go "dashboard/show/678678"
		then: "no save and delete buttons are disabled"
		waitFor {
			at DashboardShowPage
		}
		saveButton.displayed
		saveButton.getAttribute("disabled")
		deleteButton.hasClass("disabled")
		// And shareButton is not
		shareButton.displayed
		!shareButton.hasClass("disabled")
	}
}
