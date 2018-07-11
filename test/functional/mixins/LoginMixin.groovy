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
		to LoginPage
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

	def login(String u, String p, boolean rememberMe) {
		tryLogin(u, p, rememberMe)
		waitFor { at CanvasPage }
	}

	def logout() {
		// open top-screen menu if visible
		if ($(".navbar-toggle").displayed) {
			$(".navbar-toggle").click()
			waitFor { $("#navSettingsLink").displayed }
		}
		$("#navSettingsLink").click()
		waitFor { $("#navLogoutLink").displayed }

		$("#navLogoutLink").click()
		waitFor { at LoginPage }
	}
}
