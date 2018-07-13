package pages

import geb.Page

class LoginPage extends Page {

	static at = {
		waitFor {
			$("span", text: "Sign in").size() > 1
		}
	}

	static url = "login/auth"

	static content = {
		username    { $("input.loginPage_emailInput") }
		password    { $("input.loginPage_passwordInput") }
		forgotPasswordButton { $("a", text: contains("Forgot")) }
		nextButton { $("button", type: "submit") }
		rememberMeCheckbox { $("input", type: "checkbox", name: "rememberMe") }
		error { $(".inputError_inputError") }
	}
}

