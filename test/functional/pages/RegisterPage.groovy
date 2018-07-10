package pages

class RegisterPage extends GrailsPage {

    static controller = "register"
    static action = "register"

    static url = "$controller/$action"

	static at = {
		waitFor {
			nextButton.displayed
		}
	}

    static content = {
        name    { $("input", name: "name") }
        password { $("input", name: "password") }
        password2 { $("input", name: "confirmPassword") }
        timezone { $("input", name: "timezone") }
        nextButton { $("button", type: "submit") }
        agreeCheckbox { $("input", name: "toc") }
		error { $(".inputError_inputError") }
    }
}

