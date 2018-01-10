package mixins

import LoginTester1Spec
import LoginTester2Spec
import LoginTesterAdminSpec
import pages.CanvasPage
import pages.LoginPage

/**
 * Handle login / logout
 */
class LoginMixin {

	def loginTester1() {
		login(LoginTester1Spec.testerUsername, LoginTester1Spec.testerPassword)
	}
	def loginTester2() {
		login(LoginTester2Spec.testerUsername, LoginTester2Spec.testerPassword)
	}
	def loginTesterAdmin() {
		login(LoginTesterAdminSpec.testerUsername, LoginTesterAdminSpec.testerPassword)
	}

	def login(String u, String p) {
		to LoginPage
		waitFor { username.displayed }
		username << u
		password << p
		loginButton.click()
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
