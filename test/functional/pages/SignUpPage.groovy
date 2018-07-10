package pages

class SignUpPage extends GrailsPage {
	static at = {
		waitFor {
			nextButton.displayed
		}
	}

	static url = "register/signup"

	static content = {
		email { $("input", name: "email") }
		nextButton { $("button", type: "submit") }
		signUpOk { $("p", text: contains("link to your email")) }
		error { $(".inputError_inputError") }
	}
}

