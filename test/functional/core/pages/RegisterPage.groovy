package core.pages

class RegisterPage extends GrailsPage {
    
    static controller = "register"
    static action = "register"

    static url = "$controller/$action"

    static content = {
        name    { $("#name") }
        password { $("#password") }
        password2 { $("#password2") }
        loginButton { $("#loginButton") }
        agreeCheckbox { $(".checkbox-inline") }
    }
}

