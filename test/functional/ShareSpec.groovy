import core.mixins.*
import core.pages.*
import geb.spock.GebReportingSpec
import org.openqa.selenium.Keys

class ShareSpec extends GebReportingSpec {

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(LoginMixin)
		this.class.metaClass.mixin(ShareMixin)
		this.class.metaClass.mixin(NotificationMixin)
		this.class.metaClass.mixin(CanvasMixin)
		this.class.metaClass.mixin(DashboardMixin)
		this.class.metaClass.mixin(ListPageMixin)
	}

	def clickShareButton(name = "ShareSpec") {
		def row = $("a.tr").findAll { it.text().trim().startsWith(name) }.first()
		row.find(".dropdown-toggle").click()
		row.find(".share-button").click()
	}

	def save() {
		$(".sharing-dialog .save-button").click()
		waitFor { !$(".sharing-dialog") && !$(".modal-backdrop") }
	}

	def cancel() {
		$(".sharing-dialog .cancel-button").click()
		waitFor { !$(".sharing-dialog") && !$(".modal-backdrop") }
	}

	/** Cleanup helper */
	def removeStreamPermissions() {
		to StreamListPage
		clickShareButton()
		waitFor { $(".new-user-field").displayed }
		if ($(".user-delete-button").displayed) {
			waitFor {
				$(".user-delete-button").click()
				$(".access-row").size() == 0
			}
		}
		save()
	}
	/** Cleanup helper */
	def removeCanvasPermissions() {
		to CanvasListPage
		clickShareButton()
		waitFor { $(".new-user-field").displayed }
		if ($(".user-delete-button").displayed) {
			waitFor {
				$(".user-delete-button").click()
				$(".access-row").size() == 0
			}
		}
		save()
	}
	/** Cleanup helper */
	def removeDashboardPermissions() {
		to DashboardListPage
		clickShareButton()
		waitFor { $(".new-user-field").displayed }
		if ($(".user-delete-button").displayed) {
			waitFor {
				$(".user-delete-button").click()
				$(".access-row").size() == 0
			}
		}
		save()
	}

	// We still don't know why it's so hard to type text into the input,
	// just "$('.new-user-field') << text" won't work.
	// That's why this hack.
	def feedTextInput(String text) {
		waitFor {
			$('.new-user-field').displayed
			$('.new-user-field').firstElement().clear()
			$('.new-user-field') << text
			$('.new-user-field').value().equals(text)
		}
	}

	def pressKeyUntil(inputSelector, Keys key, Closure successCondition) {
		waitFor { $(inputSelector).displayed }
		def $input = $(inputSelector);
		waitFor(20, 2) {
			successCondition() ?: $input << key
		}
	}

	void "sharePopup can grant and revoke Stream permissions"() {
		loginTester1()

		when:
		to StreamListPage
		then:
		!$(".sharing-dialog")

		when: "open sharing dialog"
		clickShareButton()
		then:
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		feedTextInput("foobar")
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-danger")
		}
		then: "enter would add a permission row, but doesn't since there was error"
		$(".access-row").size() == 0
		$(".new-user-field").value() == "foobar"

		expect: "esc clears the email"
		pressKeyUntil(".new-user-field", Keys.ESCAPE) {
			!$(".new-user-field").value()
		}

		when:
		closeNotifications()
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		expect: "esc closes the popup discarding changes"
		pressKeyUntil(".new-user-field", Keys.ESCAPE) {
			!$(".sharing-dialog")
		}
		!$(".ui-pnotify")

		// ADD PERMISSION

		when: "re-open"
		waitFor { !$(".modal-backdrop") }
		clickShareButton()
		then: "access row from last time was discarded"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		feedTextInput("tester2@streamr.com")
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".access-row")
		}
		then:
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		expect: "enter saves changes when user field is empty"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "re-open once more"
		clickShareButton()
		closeNotifications()	// the second pnotify would cover the share button
		then: "check that the saved row is still there"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when:
		save()
		then: "no changes, so no message displayed"
		!$(".ui-pnotify")

		// REMOVE PERMISSION

		when: "Move to Stream page, revoke permission"
		findRow("ShareSpec").click()
		then:
		waitFor { at StreamShowPage }
		waitFor { streamMenuButton.displayed }

		when: "open menu"
		streamMenuButton.click()
		then: "shareButton in menu"
		waitFor { shareButton.displayed && !shareButton.getAttribute("disabled") }

		when:
		shareButton.click()
		then: "check that the saved row is still there"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "there should be only one row and only one delete-button..."
		$(".user-delete-button").click()
		then:
		waitFor { $(".access-row").size() == 0 }

		when: "discard changes"
		cancel()
		then: "...so no notification"
		!$(".ui-pnotify")

		when: "open menu"
		streamMenuButton.click()
		then: "shareButton in menu"
		waitFor { shareButton.displayed && !shareButton.getAttribute("disabled") }

		when: "re-open"
		shareButton.click()
		then: "check that row hasn't been deleted"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "delete again"
		$(".user-delete-button").click()
		then:
		waitFor { $(".access-row").size() == 0 }

		expect: "...this time for reals"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "open menu"
		streamMenuButton.click()
		then: "shareButton in menu"
		waitFor { shareButton.displayed && !shareButton.getAttribute("disabled") }

		when: "re-open"
		shareButton.click()
		then: "...to double-check it's gone"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "save and close"
		closeNotifications()
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			!$(".sharing-dialog")
		}
		then: "...but no changes, so no message displayed"
		!$(".ui-pnotify")

		cleanup: "just in case..."
		removeStreamPermissions()
	}

	void "sharePopup can grant and revoke Canvas permissions"() {
		loginTester1()

		when:
		to CanvasListPage
		then:
		!$(".sharing-dialog")
		waitFor { at CanvasListPage }
		waitFor { findRow("ShareSpec").displayed }

		when: "open 'ShareSpec' Canvas"
		clickShareButton()
		then:
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		feedTextInput( "foobar")
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-danger")
		}
		then: "enter adds a permission row"
		$(".access-row").size() == 0
		$(".new-user-field").value() == "foobar"

		expect: "esc clears the email"
		pressKeyUntil(".new-user-field", Keys.ESCAPE) {
			!$(".new-user-field").value()
		}

		when:
		closeNotifications()
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		when: "discard changes"
		cancel()
		then: "...so no notification"
		!$(".ui-pnotify")

		// ADD PERMISSION

		when: "re-open"
		clickShareButton()
		then: "access row from last time was discarded"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		expect: "enter saves changes when user field is empty"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "re-open once more"
		closeNotifications() // the second pnotify would cover the share button
		clickShareButton()
		then: "check that the saved row is still there"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when:
		save()
		then: "no changes, so no message displayed"
		!$(".ui-pnotify")

		// REMOVE PERMISSION

		when: "Move to Canvas info page, revoke permission"
		findRow("ShareSpec").click()
		then:
		waitFor { at CanvasPage }
		waitFor { shareButton.displayed && !shareButton.getAttribute("disabled") }

		when:
		shareButton.click()
		then: "check that the saved row is still there"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "there should be only one row and only one delete-button..."
		$(".user-delete-button").click()
		then: "it's gone!"
		waitFor { $(".access-row").size() == 0 }

		when: "discard changes"
		cancel()
		then: "...so no notification"
		!$(".ui-pnotify")

		when: "re-open"
		shareButton.click()
		then: "check that row hasn't been deleted"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "delete again"
		$(".user-delete-button").click()
		then: "it's gone!"
		waitFor { $(".access-row").size() == 0 }

		expect: "...this time for reals"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "re-open"
		closeNotifications()
		shareButton.click()
		then: "...to double-check it's gone"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "save"
		save()
		then: "...but no changes, so no message displayed"
		waitFor { !$(".sharing-dialog") }
		!$(".ui-pnotify")

		cleanup: "just in case..."
		removeCanvasPermissions()
	}

	void "sharePopup can grant and revoke Dashboard permissions"() {
		loginTester1()

		when:
		to DashboardListPage
		then:
		!$(".sharing-dialog")
		waitFor { at DashboardListPage }
		waitFor { findRow("ShareSpec").displayed }

		when: "open 'ShareSpec' Dashboard"
		clickShareButton()
		then:
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		feedTextInput( "foobar")
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-danger")
		}
		then: "no new permission row"
		$(".access-row").size() == 0
		$(".new-user-field").value() == "foobar"

		expect: "esc clears the email"
		pressKeyUntil(".new-user-field", Keys.ESCAPE) {
			!$(".new-user-field").value()
		}

		when:
		closeNotifications()
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		when: "discard changes"
		cancel()
		then: "...so no notification"
		!$(".ui-pnotify")

		// ADD PERMISSION

		when: "re-open"
		clickShareButton()
		then: "access row from last time was discarded"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		expect: "enter saves changes when user field is empty"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "re-open once more"
		closeNotifications()	// the second pnotify would cover the share button
		clickShareButton()
		then: "check that the saved row is still there"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when:
		save()
		then: "no changes, so no message displayed"
		!$(".ui-pnotify")

		// REMOVE PERMISSION

		when: "Move to Dashboard editor page, revoke permission"
		findRow("ShareSpec").click()
		then:
		waitFor { at DashboardShowPage }
		waitFor { shareButton.displayed && !shareButton.getAttribute("disabled") }

		when:
		shareButton.click()
		then: "check that the saved row is still there"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "there should be only one row and only one delete-button..."
		$(".user-delete-button").click()
		then: "it's gone!"
		waitFor { $(".access-row").size() == 0 }

		when: "discard changes"
		cancel()
		then: "...so no notification"
		!$(".ui-pnotify")

		when: "re-open"
		shareButton.click()
		then: "check that row hasn't been deleted"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "delete again"
		$(".user-delete-button").click()
		then: "it's gone!"
		waitFor { $(".access-row").size() == 0 }

		expect: "...this time for reals"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "re-open"
		closeNotifications()
		shareButton.click()
		then: "...to double-check it's gone"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "save"
		save()
		then: "...but no changes, so no message displayed"
		waitFor { !$(".sharing-dialog") }
		!$(".ui-pnotify")

		cleanup: "just in case..."
		removeDashboardPermissions()
	}

	void "read permission allows opening but doesn't show share buttons"() {
		def getStreamRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }
		def getCanvasRow = { $("a.tr").findAll { it.text().startsWith("ShareSpec") }.first() }
		def getDashboardRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }

		loginTester1()

		when: "give tester2 read permission to stream"
		to StreamListPage
		clickShareButton()
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then: "got the access-row; also it's the only one so we're not mixing things up"
		waitFor { $(".access-row").displayed }
		$(".access-row").size() == 1

		expect: "enter saves changes when user field is empty"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "give tester2 read permission to canvas"
		to CanvasListPage
		clickShareButton()
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then: "got the access-row; also it's the only one so we're not mixing things up"
		waitFor { $(".access-row") }
		$(".access-row").size() == 1

		expect: "enter saves changes when user field is empty"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "give tester2 read permission to dashboard"
		to DashboardListPage
		clickShareButton()
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then: "got the access-row; also it's the only one so we're not mixing things up"
		waitFor { $(".access-row").displayed }
		$(".access-row").size() == 1

		expect: "enter saves changes when user field is empty"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "challenger appears"
		closeNotifications()
		logout()
		loginTester2()
		to StreamListPage
		then: "no share button in list"
		!getStreamRow().find(".dropdown-toggle")

		when: "open stream edit view"
		findRow("ShareSpec").click()
		then: "there should be no menu for read rights only"
		waitFor { at StreamShowPage }
		!$("#stream-menu-toggle")

		when: "check canvas"
		to CanvasListPage
		then: "no share button in list"
		!getCanvasRow().find(".dropdown-toggle")

		when:
		findRow("ShareSpec").click()
		then: "wait until permission check is done, should not view login form (because not logging in isn't why sharing isn't allowed)"
		waitFor { at CanvasPage }
		waitFor { shareButton.hasClass("forbidden") }
		!$(".page-signin-alt #loginForm")

		when: "check dashboard"
		to DashboardListPage
		then:
		!getDashboardRow().find(".dropdown-toggle")

		when:
		findRow("ShareSpec").click()
		then: "only read rights given"
		waitFor { at DashboardShowPage }
		!$("#share-button")

		cleanup: "remove all access to ShareSpec resources"
		to StreamListPage
		logout()
		loginTester1()
		removeStreamPermissions()
		removeCanvasPermissions()
		removeDashboardPermissions()
	}

	void "write rights show stream menu but no share button"() {
		def getStreamRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }

		loginTester1()

		when: "give tester2 write permission to stream"
		to StreamListPage
		clickShareButton()
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then: "got the access-row"
		waitFor { $(".access-row").displayed }
		$(".access-row").size() == 1

		when: "toggle write rights"
		$(".permission-dropdown-toggle").click()
		$(".permission-dropdown-menu").find("li", "data-opt": "write").click()
		then:
		$(".permission-dropdown-toggle .state").text() == "can edit"

		expect: "enter saves changes when user field is empty"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "challenger appears"
		closeNotifications()
		logout()
		loginTester2()
		to StreamListPage
		then: "no share button in list"
		getStreamRow().find(".dropdown-toggle").click()
		!getStreamRow().find(".share-button")

		when: "open stream edit view"
		findRow("ShareSpec").click()
		then:
		waitFor { at StreamShowPage }

		when: "open menu"
		streamMenuButton.click()
		then: "no shareButton in menu!"
		waitFor { deleteStreamButton.displayed }
		!$("#share-button")

		cleanup:
		to StreamListPage	// closes dialog if open...
		logout()
		loginTester1()
		removeStreamPermissions()
	}

	void "shared stream is shown in search box"() {
		loginTester1()

		when:
		to StreamListPage
		clickShareButton()
		waitFor { $(".new-user-field").displayed }
		feedTextInput("tester2@streamr.com")
		$(".new-user-button").click()
		then: "got the access-row; also it's the only one so we're not mixing things up"
		waitFor { $(".access-row") }
		$(".access-row").size() == 1

		expect: "enter saves changes when user field is empty"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "try search"
		closeNotifications()
		logout()

		loginTester2()

		search << "ShareSp"
		then: "found!"
		waitFor { $('.streamr-search-suggestion .streamr-search-suggestion-name', text: "ShareSpec") }

		cleanup: "remove tester2 permission"
		to CanvasListPage	// hard-close the dialog if open (cleanup can be invoked elsewhere)
		logout()
		loginTester1()
		removeStreamPermissions()
	}

	void "public stream is visible in search and can be inspected, but won't be shown in list"() {
		loginTester1()

		when: "publish it if not public (defensive, but we aren't testing that DB state is correct...)"
		to StreamListPage
		clickShareButton()
		waitFor { $(".modal-body .owner-row .switcher").displayed }
		if (!$(".anonymous-switcher").attr("checked")) {
			$(".modal-body .owner-row .switcher").click()
		}
		then:
		$(".anonymous-switcher").attr("checked")

		when: "try search"
		save()
		closeNotifications()
		def streamShowUrl = findRow("ShareSpec").attr("href")
		logout()

		loginTester2()

		search << "ShareSp"
		then: "found!"
		waitFor { $('.streamr-search-suggestion .streamr-search-suggestion-name', text: "ShareSpec") }

		when: "check list"
		to StreamListPage
		then: "not found"
		findRow("ShareSpec").size() == 0

		when: "inspect"
		go streamShowUrl
		then:
		waitFor { at StreamShowPage }

		cleanup: "un-publish stream"
		to CanvasListPage	// hard-close the dialog if open (cleanup can be invoked elsewhere)
		logout()

		loginTester1()

		to StreamListPage
		clickShareButton()
		waitFor { $(".modal-body .owner-row .switcher").displayed }
		if ($(".anonymous-switcher").attr("checked")) {
			$(".modal-body .owner-row .switcher").click()
		}
		save()
	}

	void "access to dashboard is enough for viewing it"() {
		def name = "ShareSpec_"+System.currentTimeMillis()

		loginTester1()

		// Create test canvas
		to CanvasPage
		addAndWaitModule("Button")
		addAndWaitModule("Table")
		moveModuleBy("Table", 300, 0)
		connectEndpoints(findOutput("Button", "out"), findInput("Table", "input1"))
		ensureRealtimeTabDisplayed()
		saveCanvasAs(name)
		startCanvas(true)

		// Create test dashboard
		createDashboard(name)
		addDashboardItem(name, "Table")
		addDashboardItem(name, "Button")
		saveDashboard()

		// Share to tester2
		shareButton.click()
		waitForShareDialog()
		shareTo("tester2@streamr.com")
		closeNotifications()

		when:
		findDashboardItem("Button").find(".button-module-button").click()

		then:
		waitFor {
			findDashboardItem("Table").find(".event-table-module-content tbody tr").size() > 0
		}

		when:
		closeNotifications()
		logout()
		loginTester2()
		to DashboardListPage
		$(".tr", text:contains(name)).click()
		waitFor { at DashboardShowPage }

		then:
		waitFor {
			findDashboardItem("Table").find(".event-table-module-content tbody tr").size() > 0
			!findErrorNotification().displayed
		}

	}
}
