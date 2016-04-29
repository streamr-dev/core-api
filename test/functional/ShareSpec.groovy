import core.mixins.LoginMixin
import core.pages.CanvasListPage
import core.pages.CanvasPage
import core.pages.DashboardListPage
import core.pages.DashboardShowPage
import core.pages.StreamListPage
import core.pages.StreamShowPage
import geb.spock.GebReportingSpec
import org.openqa.selenium.Keys
import org.openqa.selenium.StaleElementReferenceException;

class ShareSpec extends GebReportingSpec {

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(LoginMixin)
	}

	def closePnotify() {
		$(".ui-pnotify-closer").each {
			try { it.click() } catch (StaleElementReferenceException e) {}
		}
		waitFor { !$(".ui-pnotify").displayed }
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
		def getStreamRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }
		to StreamListPage
		getStreamRow().find("button").click()
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
		def getCanvasRow = { $("a.tr").findAll { it.text().startsWith("ShareSpec") }.first() }
		to CanvasListPage
		getCanvasRow().find("button").click()
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
		def getDashboardRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }
		to DashboardListPage
		getDashboardRow().find("button").click()
		waitFor { $(".new-user-field").displayed }
		if ($(".user-delete-button").displayed) {
			waitFor {
				$(".user-delete-button").click()
				$(".access-row").size() == 0
			}
		}
		save()
	}

	// fix weird bug: on Jenkins machine and for particular test, only "tester2" is typed for
	//   $(".new-user-field") << "tester2@streamr.com"
	def forceFeedTextInput(inputSelector, String text) {
		waitFor { $(inputSelector).displayed }
		def $input = $(inputSelector);
		waitFor(20, 1) {
			def len = $input.getAttribute("value").length()
			len >= text.length() ?: ($input << text.substring(len))
		}
		return $input
	}

	def pressKeyUntil(inputSelector, Keys key, Closure successCondition) {
		waitFor { $(inputSelector).displayed }
		def $input = $(inputSelector);
		waitFor(20, 2) {
			successCondition() ?: $input << key
		}
	}

	void "sharePopup can grant and revoke Stream permissions"() {
		def getStreamRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }
		loginTester1()

		when:
		to StreamListPage
		then:
		!$(".sharing-dialog")

		when: "open 'ShareSpec' Stream"
		getStreamRow().find("button").click()
		then:
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		forceFeedTextInput(".new-user-field", "foobar")
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
		closePnotify()
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		getStreamRow().find("button").click()
		then: "access row from last time was discarded"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		closePnotify()	// the second pnotify would cover the share button
		getStreamRow().find("button").click()
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
		getStreamRow().click()
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
		closePnotify()
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			!$(".sharing-dialog")
		}
		then: "...but no changes, so no message displayed"
		!$(".ui-pnotify")

		cleanup: "just in case..."
		removeStreamPermissions()
	}

	void "sharePopup can grant and revoke Canvas permissions"() {
		def getCanvasRow = { $("a.tr").findAll { it.text().startsWith("ShareSpec") }.first() }
		loginTester1()

		when:
		to CanvasListPage
		then:
		!$(".sharing-dialog")
		waitFor { at CanvasListPage }
		waitFor { getCanvasRow().displayed }

		when: "open 'ShareSpec' Canvas"
		getCanvasRow().find("button").click()
		then:
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		forceFeedTextInput(".new-user-field", "foobar")
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
		closePnotify()
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		getCanvasRow().find("button").click()
		then: "access row from last time was discarded"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		closePnotify()	// the second pnotify would cover the share button
		getCanvasRow().find("button").click()
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
		getCanvasRow().click()
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
		closePnotify()
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
		def getDashboardRow = { $("a.tr").findAll { it.text().startsWith("ShareSpec") }.first() }
		loginTester1()

		when:
		to DashboardListPage
		then:
		!$(".sharing-dialog")
		waitFor { at DashboardListPage }
		waitFor { getDashboardRow().displayed }

		when: "open 'ShareSpec' Dashboard"
		getDashboardRow().find("button").click()
		then:
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		forceFeedTextInput(".new-user-field", "foobar")
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
		closePnotify()
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		getDashboardRow().find("button").click()
		then: "access row from last time was discarded"
		waitFor { $(".sharing-dialog") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		closePnotify()	// the second pnotify would cover the share button
		getDashboardRow().find("button").click()
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
		getDashboardRow().click()
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
		closePnotify()
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
		getStreamRow().find("button").click()
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		getCanvasRow().find("button").click()
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		getDashboardRow().find("button").click()
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		closePnotify()
		logout()
		loginTester2()
		to StreamListPage
		then: "no share button in list"
		!getStreamRow().find("button")

		when: "open stream edit view"
		getStreamRow().click()
		then: "there should be no menu for read rights only"
		waitFor { at StreamShowPage }
		!$("#stream-menu-toggle")

		when: "check canvas"
		to CanvasListPage
		then: "no share button in list"
		!getCanvasRow().find("button")

		when:
		getCanvasRow().click()
		then: "wait until permission check is done, should not view login form (because not logging in isn't why sharing isn't allowed)"
		waitFor { at CanvasPage }
		waitFor { shareButton.hasClass("forbidden") }
		!$(".page-signin-alt #loginForm")

		when: "check dashboard"
		to DashboardListPage
		then:
		!getDashboardRow().find("button")

		when:
		getDashboardRow().click()
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
		getStreamRow().find("button").click()
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
		$(".new-user-button").click()
		then: "got the access-row"
		waitFor { $(".access-row").displayed }
		$(".access-row").size() == 1

		when: "toggle write rights"
		$(".permission-dropdown").click()
		$(".permission-dropdown").find("li", "data-opt": "write").click()
		then:
		$(".permission-dropdown .access-description").text() == "can edit"

		expect: "enter saves changes when user field is empty"
		pressKeyUntil(".new-user-field", Keys.ENTER) {
			$(".ui-pnotify .alert-success")
		}
		waitFor { !$(".sharing-dialog") }

		when: "challenger appears"
		closePnotify()
		logout()
		loginTester2()
		to StreamListPage
		then: "no share button in list"
		!getStreamRow().find("button")

		when: "open stream edit view"
		getStreamRow().click()
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
		def getStreamRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }

		loginTester1()

		when:
		to StreamListPage
		getStreamRow().find("button").click()
		waitFor { $(".new-user-field").displayed }
		forceFeedTextInput(".new-user-field", "tester2@streamr.com")
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
		closePnotify()
		logout()

		loginTester2()

		search << "ShareSp"
		then: "found!"
		waitFor { $('.tt-suggestion .tt-suggestion-name', text: "ShareSpec") }

		cleanup: "remove tester2 permission"
		to CanvasListPage	// hard-close the dialog if open (cleanup can be invoked elsewhere)
		logout()
		loginTester1()
		removeStreamPermissions()
	}

	void "public stream is visible in search and can be inspected, but won't be shown in list"() {
		def getStreamRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }

		loginTester1()

		when: "publish it if not public (defensive, but we aren't testing that DB state is correct...)"
		to StreamListPage
		getStreamRow().find("button").click()
		waitFor { $(".modal-body .owner-row .switcher").displayed }
		if (!$(".anonymous-switcher").attr("checked")) {
			$(".modal-body .owner-row .switcher").click()
		}
		then:
		$(".anonymous-switcher").attr("checked")

		when: "try search"
		save()
		closePnotify()
		def streamShowUrl = getStreamRow().attr("href")
		logout()

		loginTester2()

		search << "ShareSp"
		then: "found!"
		waitFor { $('.tt-suggestion .tt-suggestion-name', text: "ShareSpec") }

		when: "check list"
		to StreamListPage
		then: "not found"
		getStreamRow().size() == 0

		when: "inspect"
		go streamShowUrl
		then:
		waitFor { at StreamShowPage }

		cleanup: "un-publish stream"
		to CanvasListPage	// hard-close the dialog if open (cleanup can be invoked elsewhere)
		logout()

		loginTester1()

		to StreamListPage
		getStreamRow().find("button").click()
		waitFor { $(".modal-body .owner-row .switcher").displayed }
		if ($(".anonymous-switcher").attr("checked")) {
			$(".modal-body .owner-row .switcher").click()
		}
		save()
	}
}
