package pages

class SignUpPage extends GrailsPage {

	static controller = "register"
	static action = "signup"
	
	static url = "register/signup"

	static content = {
            email    { $("#username") }
            signUpButton { $("#signUpButton") }
            signUpOk { $("#signup-ok") }
	}

}

