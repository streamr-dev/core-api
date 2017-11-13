package core.pages

import geb.Page

class SignUpPage extends GrailsPage {

	static controller = "register"
	static action = "signup"
	
	static url = "register/signup"

	static content = {
            email    { $("#username") }
            signUpButton { $("#loginButton") }
            signUpOk { $("#signup-ok") }
	}

}

