package core.mixins

import com.unifina.domain.security.Permission

/**
 * For handling sharing dialog
 */
@Mixin(NotificationMixin)
class ShareMixin {

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

	def getSharingDialog() {
		return $(".sharing-dialog")
	}

	void waitForShareDialog() {
		waitFor {
			sharingDialog.displayed
		}
	}

	def findAccessRow(String username) {
		return sharingDialog.find(".access-row", text:contains(username))
	}

	void setPermission(String username, Permission.Operation op) {
		def row = findAccessRow(username)
		row.find('.permission-dropdown').click()
		row.find(".permission-dropdown li", "data-opt": op.id).click()
	}

	void acceptShareDialog() {
		sharingDialog.find('.save-button').click()
		waitFor { !sharingDialog.displayed }
	}

	void shareTo(String username, Permission.Operation op = Permission.Operation.READ) {
		forceFeedTextInput(".sharing-dialog .new-user-field", username)
		$(".sharing-dialog .new-user-button").click()
		setPermission(username, op)
		acceptShareDialog()
		waitForSuccessNotification()
	}
}
