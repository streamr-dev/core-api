import core.LoginTester1Spec
import core.pages.CanvasListPage
import core.pages.StreamListPage
import core.pages.StreamShowPage
import org.openqa.selenium.Keys;

class ShareSpec extends LoginTester1Spec {

	void "sharePopup can grant and revoke Stream permissions"() {
		when:
		to StreamListPage
		then:
		!$(".bootbox.modal")

		when: "open 'ShareSpec' Stream"
		$(".tr", "data-id": "134").find("button").click()
		then:
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "add invalid email"
		$(".new-user-field") << "foobar" << Keys.ENTER
		then: "enter adds a permission row"
		waitFor { $(".ui-pnotify").find(".alert-danger") }
		$(".access-row").size() == 0
		$(".new-user-field").value() == "foobar"

		when:
		$(".new-user-field") << Keys.ESCAPE
		then: "esc clears the email"
		!$(".new-user-field").value()

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
		$(".tr", "data-id": "134").find("button").click()
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
		waitFor { $(".ui-pnotify").find(".alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "close pnotify-popups, otherwise the second pnotify covers the share button..."
		$(".ui-pnotify-closer").each { it.click() }
		then:
		waitFor { $(".ui-pnotify").size() == 0 }

		when: "re-open once more"
		$(".tr", "data-id": "134").find("button").click()
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
		$(".tr", "data-id": "134").click()
		then:
		waitFor { at StreamShowPage }
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
		waitFor { $(".ui-pnotify").find(".alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "re-open"
		shareButton.click()
		then: "...to double-check it's gone"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 0

		when: "close pnotify-popup"
		$(".ui-pnotify-closer").each { it.click() }
		then:
		waitFor { $(".ui-pnotify").size() == 0 }

		when: "save"
		$(".new-user-field") << Keys.ENTER
		then: "...but no changes, so no message displayed"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")
	}

	void "sharePopup can grant and revoke Canvas permissions"() {
		def getCanvasRow = { $("a.tr").findAll { it.text().startsWith("ShareSpec") }.first() }

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
		waitFor { $(".ui-pnotify").find(".alert-danger") }
		$(".access-row").size() == 0
		$(".new-user-field").value() == "foobar"

		when:
		$(".new-user-field") << Keys.ESCAPE
		then: "esc clears the email"
		!$(".new-user-field").value()

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
		waitFor { $(".ui-pnotify").find(".alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "close pnotify-popups, otherwise the second pnotify covers the share button..."
		$(".ui-pnotify-closer").each { it.click() }
		then:
		waitFor { $(".ui-pnotify").size() == 0 }

		when: "re-open once more"
		getCanvasRow().find("button").click()
		then: "check that the saved row is still there"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1

		/* TODO: add share button to Canvas page
		when:
		$("button", text: "Save").click()
		then: "no changes, so no message displayed"
		waitFor { !$(".bootbox.modal") }
		!$(".ui-pnotify")

		// REMOVE PERMISSION

		when: "Move to Canvas info page, revoke permission"
		getCanvasRow().click()
		then:
		waitFor { at CanvasShowPage }
		waitFor { shareButton.displayed }

		when:
		shareButton.click()
		then: "check that the saved row is still there"
		waitFor { $(".bootbox.modal") }
		waitFor { $(".new-user-field").displayed }
		$(".access-row").size() == 1
		*/

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
		//shareButton.click()
		getCanvasRow().find("button").click()
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
		waitFor { $(".ui-pnotify").find(".alert-success") }
		waitFor { !$(".bootbox.modal") }

		when: "close 'saving successful'-popup"
		$(".ui-pnotify-closer").each { it.click() }
		then:
		waitFor { $(".ui-pnotify").size() == 0 }

		when: "re-open"
		//shareButton.click()
		getCanvasRow().find("button").click()
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
}