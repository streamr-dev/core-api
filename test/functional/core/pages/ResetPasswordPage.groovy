package core.pages

class ResetPasswordPage extends GrailsPage {
	static controller = "register"
	static action = "resetPassword"

	static url = "$controller/$action"

	static content = {
		password    { $("#password") }
		password2 { $("#password2") }
		resetButton { $("#reset") }
	}

}

