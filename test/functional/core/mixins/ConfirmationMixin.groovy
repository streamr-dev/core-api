package core.mixins

class ConfirmationMixin {
	def waitForConfirmation(String dialogClass = ".modal-dialog") {
		waitFor { $(dialogClass).displayed }
	}

	def acceptConfirmation(String dialogClass = ".modal-dialog") {
		$("$dialogClass .btn-primary").findAll { it.displayed }.click()
		waitFor {
			$(dialogClass).findAll { it.displayed }.empty
			$(".modal-backdrop").empty
		}
	}
}
