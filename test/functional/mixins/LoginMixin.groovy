package mixins

import pages.CanvasPage
import pages.LoginPage

/**
 * Handle login / logout
 */
trait LoginMixin {

	def loginTester1() {
		login(LoginTester1Spec.testerUsername, LoginTester1Spec.testerPassword)
	}

	def loginTester2() {
		login(LoginTester2Spec.testerUsername, LoginTester2Spec.testerPassword)
	}

	def loginTesterAdmin() {
		login(LoginTesterAdminSpec.testerUsername, LoginTesterAdminSpec.testerPassword)
	}

	def tryLogin(String u, String p, boolean rememberMe = false) {
		waitFor { username.displayed }
		username << u
		waitFor { nextButton.click() }
		waitFor { password.displayed }
		password << p
		if (rememberMe) {
			rememberMeCheckbox.click()
		}
		waitFor { nextButton.click() }
	}

	def login(String u, String p, boolean rememberMe = false) {
		logout()
		to LoginPage
		tryLogin(u, p, rememberMe)
		waitFor { at CanvasPage }
	}

	def logout() {
		go "logout"
		waitFor { at LoginPage }
	}
}
