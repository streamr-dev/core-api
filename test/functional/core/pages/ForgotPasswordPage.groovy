package core.pages

class ForgotPasswordPage extends GrailsPage {
	static controller = "register"
	static action = "forgotPassword"

	static url = "$controller/$action"

	static content = {
		username    { $("#username") }
		sendButton { $("#reset") }
	}

}

