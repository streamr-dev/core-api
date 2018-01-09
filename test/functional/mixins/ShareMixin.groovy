package mixins

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
	def feedTextInput(String selector, String text) {
		waitFor {
			$(selector).displayed
			$(selector).firstElement().clear()
			$(selector) << text
			$(selector).value().equals(text)
		}
	}
	def feedTextInput(String text) {
		feedTextInput('.new-user-field', text)
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
	}

	void shareTo(String username, Permission.Operation op = Permission.Operation.READ) {
		feedTextInput(username)
		$(".sharing-dialog .new-user-button").click()
		setPermission(username, op)
		acceptShareDialog()
		waitForSuccessNotification()
	}

	void shareToInReact(String username, Permission.Operation op = Permission.Operation.READ) {
		feedTextInput(".shareDialogInputRow_inputRow input", username)
		$(".shareDialogInputRow_addButton").click()
		def row = $(".modal-dialog").find(".shareDialogPermission_userLabel", text:contains(username))
		row.find('.shareDialogPermission_select').click()
		row.find(".shareDialogPermission_select .Select-option", "text": contains(op.id)).click()
		acceptShareDialog()
		$(".shareDialogFooter_saveButton").click()
		waitForSuccessNotification()
		waitFor { !$(".modal-dialog").displayed }
	}
}
