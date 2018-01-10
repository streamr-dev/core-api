package pages

class ChangePasswordPage extends GrailsPage {

	static controller = "profile"
	static action = "changePwd"

	static url = "$controller/$action"

	static content = {
		navbar { module NavbarModule }

		currentPassword { $("#currentpassword") }
		newPassword { $("#password") }
		newPasswordAgain { $("#password2") }
		
		changePassword { $("#submit") }
	}
}


