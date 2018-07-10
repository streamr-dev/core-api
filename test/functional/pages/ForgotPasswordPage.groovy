package pages

class ForgotPasswordPage extends GrailsPage {
	static at = {
		waitFor {
			sendButton.displayed
		}
	}

	static url = "register/forgotPassword"

	static content = {
		username    { $("input", name: "email") }
		sendButton { $("button", type: "submit") }
		error { $(".inputError_inputError") }
	}

}

