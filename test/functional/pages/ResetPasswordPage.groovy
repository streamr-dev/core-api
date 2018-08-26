package pages

class ResetPasswordPage extends GrailsPage {
	static at = {
		waitFor {
			password.displayed || password2.displayed
		}
	}

	static url = "register/resetPassword"

	static content = {
		password    { $("input", name: "password") }
		password2 { $("input", name: "confirmPassword") }
		nextButton { $("button", type: "submit") }
		error { $(".inputError_root") }
	}

}

