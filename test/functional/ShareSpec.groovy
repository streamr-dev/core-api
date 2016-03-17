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
		waitFor { $(".ui-pnotify").size() == 0 }
	}

	void "sharePopup can grant and revoke Stream permissions"() {
		def getStreamRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }
		loginTester1()

		when:
		to StreamListPage
		then:
		!$(".bootbox.modal")

		when: "open 'ShareSpec' Stream"
		getStreamRow().find("button").click()
		then:
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		$(".new-user-field") << "foobar" << Keys.ENTER
		then: "enter adds a permission row"
		waitFor { $(".ui-pnotify .alert-danger") }
		$(".access-row").size() == 0
		$(".new-user-field").value() == "foobar"

		when:
		$(".new-user-field") << Keys.ESCAPE
		then: "esc clears the email"
		waitFor { !$(".new-user-field").value() }

		when:
		$(".new-user-field") << "tester2@streamr.com"
		$(".new-user-button").click()
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		when:
		$(".new-user-field") << Keys.ESCAPE
		then: "esc closes the popup discarding changes"
		waitFor { !$(".bootbox.modal") }

		// ADD PERMISSION

		when: "re-open"
		getStreamRow().find("button").click()
		then: "access row from last time was discarded"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		$(".new-user-field") << "tester2@streamr.com" << Keys.ENTER
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		when:
		$(".new-user-field") << Keys.ENTER
		then: "enter saves changes when user field is empty"
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "re-open once more"
		closePnotify()	// the second pnotify would cover the share button
		getStreamRow().find("button").click()
		then: "check that the saved row is still there"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when:
		$("button", text: "Save").click()
		then: "no changes, so no message displayed"
		waitFor { !$(".bootbox.modal") }
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
		waitFor { shareButton.displayed }

		when:
		shareButton.click()
		then: "check that the saved row is still there"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "there should be only one row and only one delete-button..."
		$(".user-delete-button").click()
		then:
		waitFor { $(".access-row").size() == 0 }

		when: "discard changes"
		$("button", text: "Cancel").click()
		then: "...so no notification"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")

		when: "open menu"
		streamMenuButton.click()
		then: "shareButton in menu"
		waitFor { shareButton.displayed }

		when: "re-open"
		shareButton.click()
		then: "check that row hasn't been deleted"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "delete again"
		$(".user-delete-button").click()
		then:
		waitFor { $(".access-row").size() == 0 }

		when: "...this time for reals"
		$(".new-user-field") << Keys.ENTER
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "open menu"
		streamMenuButton.click()
		then: "shareButton in menu"
		waitFor { shareButton.displayed }

		when: "re-open"
		shareButton.click()
		then: "...to double-check it's gone"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "save"
		closePnotify()
		$(".new-user-field") << Keys.ENTER
		then: "...but no changes, so no message displayed"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")
	}

	void "sharePopup can grant and revoke Canvas permissions"() {
		def getCanvasRow = { $("a.tr").findAll { it.text().startsWith("ShareSpec") }.first() }
		loginTester1()

		when:
		to CanvasListPage
		then:
		!$(".bootbox.modal")
		waitFor { at CanvasListPage }
		waitFor { getCanvasRow().displayed }

		when: "open 'ShareSpec' Canvas"
		getCanvasRow().find("button").click()
		then:
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		$(".new-user-field") << "foobar" << Keys.ENTER
		then: "enter adds a permission row"
		waitFor { $(".ui-pnotify .alert-danger") }
		$(".access-row").size() == 0
		$(".new-user-field").value() == "foobar"

		when:
		$(".new-user-field") << Keys.ESCAPE
		then: "esc clears the email"
		waitFor { !$(".new-user-field").value() }

		when:
		$(".new-user-field") << "tester2@streamr.com"
		$(".new-user-button").click()
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		when:
		$(".new-user-field") << Keys.ESCAPE
		then: "esc closes the popup discarding changes"
		waitFor { !$(".bootbox.modal") }

		// ADD PERMISSION

		when: "re-open"
		getCanvasRow().find("button").click()
		then: "access row from last time was discarded"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		$(".new-user-field") << "tester2@streamr.com" << Keys.ENTER
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		when:
		$(".new-user-field") << Keys.ENTER
		then: "enter saves changes when user field is empty"
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "re-open once more"
		closePnotify()	// the second pnotify would cover the share button
		getCanvasRow().find("button").click()
		then: "check that the saved row is still there"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when:
		$("button", text: "Save").click()
		then: "no changes, so no message displayed"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")

		// REMOVE PERMISSION

		when: "Move to Canvas info page, revoke permission"
		getCanvasRow().click()
		then:
		waitFor { at CanvasPage }
		waitFor { shareButton.displayed }

		when:
		shareButton.click()
		then: "check that the saved row is still there"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "there should be only one row and only one delete-button..."
		$(".user-delete-button").click()
		then: "it's gone!"
		waitFor { $(".access-row").size() == 0 }

		when: "discard changes"
		$("button", text: "Cancel").click()
		then: "...so no notification"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")

		when: "re-open"
		shareButton.click()
		then: "check that row hasn't been deleted"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "delete again"
		$(".user-delete-button").click()
		then: "it's gone!"
		waitFor { $(".access-row").size() == 0 }

		when: "...this time for reals"
		$(".new-user-field") << Keys.ENTER
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "re-open"
		closePnotify()
		shareButton.click()
		then: "...to double-check it's gone"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "save"
		$(".new-user-field") << Keys.ENTER
		then: "...but no changes, so no message displayed"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")
	}

	void "sharePopup can grant and revoke Dashboard permissions"() {
		def getDashboardRow = { $("a.tr").findAll { it.text().startsWith("ShareSpec") }.first() }
		loginTester1()

		when:
		to DashboardListPage
		then:
		!$(".bootbox.modal")
		waitFor { at DashboardListPage }
		waitFor { getDashboardRow().displayed }

		when: "open 'ShareSpec' Dashboard"
		getDashboardRow().find("button").click()
		then:
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		$(".new-user-field") << "foobar" << Keys.ENTER
		then: "enter adds a permission row"
		waitFor { $(".ui-pnotify .alert-danger") }
		$(".access-row").size() == 0
		$(".new-user-field").value() == "foobar"

		when:
		$(".new-user-field") << Keys.ESCAPE
		then: "esc clears the email"
		waitFor { !$(".new-user-field").value() }

		when:
		$(".new-user-field") << "tester2@streamr.com"
		$(".new-user-button").click()
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		when:
		$(".new-user-field") << Keys.ESCAPE
		then: "esc closes the popup discarding changes"
		waitFor { !$(".bootbox.modal") }

		// ADD PERMISSION

		when: "re-open"
		getDashboardRow().find("button").click()
		then: "access row from last time was discarded"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		$(".new-user-field") << "tester2@streamr.com" << Keys.ENTER
		then:
		waitFor { $(".access-row") }
		$(".access-row").size() == 1
		!$(".new-user-field").value()

		when:
		$(".new-user-field") << Keys.ENTER
		then: "enter saves changes when user field is empty"
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "re-open once more"
		closePnotify()	// the second pnotify would cover the share button
		getDashboardRow().find("button").click()
		then: "check that the saved row is still there"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when:
		$("button", text: "Save").click()
		then: "no changes, so no message displayed"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")

		// REMOVE PERMISSION

		when: "Move to Dashboard editor page, revoke permission"
		getDashboardRow().click()
		then:
		waitFor { at DashboardShowPage }
		waitFor { shareButton.displayed }

		when:
		shareButton.click()
		then: "check that the saved row is still there"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "there should be only one row and only one delete-button..."
		$(".user-delete-button").click()
		then: "it's gone!"
		waitFor { $(".access-row").size() == 0 }

		when: "discard changes"
		$("button", text: "Cancel").click()
		then: "...so no notification"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")

		when: "re-open"
		shareButton.click()
		then: "check that row hasn't been deleted"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		when: "delete again"
		$(".user-delete-button").click()
		then: "it's gone!"
		waitFor { $(".access-row").size() == 0 }

		when: "...this time for reals"
		$(".new-user-field") << Keys.ENTER
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "re-open"
		closePnotify()
		shareButton.click()
		then: "...to double-check it's gone"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "save"
		$(".new-user-field") << Keys.ENTER
		then: "...but no changes, so no message displayed"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")
	}

	void "read permission allows opening but doesn't show share buttons"() {
		def getStreamRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }
		def getCanvasRow = { $("a.tr").findAll { it.text().startsWith("ShareSpec") }.first() }
		def getDashboardRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }

		loginTester1()

		when: "give tester2 read permission to stream"
		to StreamListPage
		getStreamRow().find("button").click()
		then:
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when:
		$(".new-user-field") << "tester2@streamr.com" << Keys.ENTER
		then: "got the access-row; also it's the only one so we're not mixing things up"
		waitFor { $(".access-row").displayed }
		$(".access-row").size() == 1

		when: "save stream read right"
		$(".new-user-field") << Keys.ENTER
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "give tester2 read permission to canvas"
		to CanvasListPage
		getCanvasRow().find("button").click()
		waitFor { $(".new-user-field").displayed }
		$(".new-user-field") << "tester2@streamr.com" << Keys.ENTER
		then: "got the access-row; also it's the only one so we're not mixing things up"
		waitFor { $(".access-row") }
		$(".access-row").size() == 1

		when: "save canvas read right"
		$(".new-user-field") << Keys.ENTER
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "give tester2 read permission to dashboard"
		to DashboardListPage
		getDashboardRow().find("button").click()
		waitFor { $(".new-user-field").displayed }
		$(".new-user-field") << "tester2@streamr.com" << Keys.ENTER
		then: "got the access-row; also it's the only one so we're not mixing things up"
		waitFor { $(".access-row").displayed }
		$(".access-row").size() == 1

		when: "save dashboard read right"
		$(".new-user-field") << Keys.ENTER
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "challenger appears"
		closePnotify()
		logout()
		loginTester2()
		to StreamListPage
		then: "no share button in list"
		!getStreamRow().find("button")

		when: "open edit view"
		getStreamRow().click()
		then:
		waitFor { at StreamShowPage }

		when: "open menu"
		streamMenuButton.click()
		then: "no shareButton in menu!"
		waitFor { deleteStreamButton.displayed }
		!$("#share-button")

		when: "check canvas"
		to CanvasListPage
		then: "no share button in list"
		!getCanvasRow().find("button")

		when:
		getCanvasRow().click()
		then: "only read rights given"
		waitFor { at CanvasPage }
		shareButton.disabled

		when: "check dashboard"
		to DashboardListPage
		then:
		getDashboardRow().click()

		when:
		getDashboardRow().click()
		then: "only read rights given"
		waitFor { at DashboardShowPage }
		!$("#share-button")

		// REMOVE ACCESS
		logout()
		loginTester1()

		when:
		to StreamListPage
		getStreamRow().find("button").click()
		waitFor { $(".user-delete-button").displayed }
		then: "got the access-row; also it's the only one so we're not mixing things up"
		$(".access-row").size() == 1

		when: "there should be only one delete-button..."
		$(".user-delete-button").click()
		then: "gone!"
		waitFor { $(".access-row").size() == 0 }

		when:
		$("button", text: "Save").click()
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when:
		to CanvasListPage
		getCanvasRow().find("button").click()
		waitFor { $(".user-delete-button").displayed }
		then: "got the access-row; also it's the only one so we're not mixing things up"
		$(".access-row").size() == 1

		when: "there should be only one delete-button..."
		$(".user-delete-button").click()
		then: "gone!"
		waitFor { $(".access-row").size() == 0 }

		when:
		$("button", text: "Save").click()
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

		when:
		to DashboardListPage
		getDashboardRow().find("button").click()
		waitFor { $(".user-delete-button").displayed }
		then: "got the access-row; also it's the only one so we're not mixing things up"
		$(".access-row").size() == 1

		when: "there should be only one delete-button..."
		$(".user-delete-button").click()
		then: "gone!"
		waitFor { $(".access-row").size() == 0 }

		when:
		$("button", text: "Save").click()
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }
	}

	void "shared stream is shown in search box"() {
		def getStreamRow = { $("a.tr").findAll { it.text().trim().startsWith("ShareSpec") }.first() }

		loginTester1()

		when: "give tester2 read permission to stream"
		to StreamListPage
		getStreamRow().find("button").click()
		waitFor { $(".new-user-field").displayed }
		$(".new-user-field") << "tester2@streamr.com" << Keys.ENTER
		then: "got the access-row; also it's the only one so we're not mixing things up"
		waitFor { $(".access-row") }
		$(".access-row").size() == 1

		when: "save canvas read right"
		$(".new-user-field") << Keys.ENTER
		then:
		waitFor { $(".ui-pnotify .alert-success") }
		waitFor { !$(".bootbox.modal") }

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

		to StreamListPage
		getStreamRow().find("button").click()
		waitFor { $(".user-delete-button").displayed }
		$(".user-delete-button").click()
		waitFor { $(".access-row").size() == 0 }
		$("button", text: "Save").click()
		waitFor { $(".ui-pnotify .alert-success") }
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

		when:
		$("button", text: "Save").click()
		then:
		//waitFor { $(".ui-pnotify .alert-success") }	// robustness...
		waitFor { !$(".bootbox.modal") }

		when: "try search"
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
		$("button", text: "Save").click()
	}
}
