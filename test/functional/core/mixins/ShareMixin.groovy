package core.mixins

import com.unifina.domain.security.Permission

/**
 * For handling sharing dialog
 */
@Mixin(NotificationMixin)
class ShareMixin {

	// We still don't know why it's so hard to type text into the input,
	// just "$('.new-user-field') << text" won't work.
	// That's why this hack.
	// The same is used in ShareSpec.groovy
	def feedTextInput(String text) {
		waitFor {
			$('.new-user-field').displayed
			$('.new-user-field').firstElement().clear()
			$('.new-user-field') << text
			$('.new-user-field').value().equals(text)
		}
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
		feedTextInput(username)
		$(".sharing-dialog .new-user-button").click()
		setPermission(username, op)
		acceptShareDialog()
		waitForSuccessNotification()
	}

}
