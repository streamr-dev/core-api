package pages

import geb.Page

class LoginPage extends Page {

	static at = {
		waitFor {
			$("#username").displayed
			$("#password").displayed
		}
	}
	
	static url = "login/auth"

	static content = {
		username    { $("#username") }
		password    { $("#password") }
		loginButton { $("#loginButton") }
		rememberMeCheckbox { $("#rememberMeCheckbox") }
	}

}

