import geb.spock.GebReportingSpec
import mixins.LoginMixin
import pages.*

class ForgotPasswordSpec extends GebReportingSpec implements LoginMixin {

	def "go to forgotPasswordPage"(){
		setup:
		to LoginPage
		username = "random@user.name"
		nextButton.click()

		when: "Clicked 'Forgot password'"
		waitFor {
			forgotPasswordButton.displayed
			forgotPasswordButton.click()
		}
		then: "ForgotPasswordPage opened"
		at ForgotPasswordPage
	}

	def "weak password is not accepted"() {
		when: "go to the URL normally found from email"
		to ResetPasswordPage, "?t=ForgotPasswordSpec"
		then: "reset password page must be shown"
		at ResetPasswordPage

		when: "a weak password is given"
		password << "weakPassword"
		nextButton.click()
		then: "it must not be accepted"
		waitFor {
			at ResetPasswordPage
			error.displayed
		}
	}

	def "password reset flow"() {
		// TODO: add setup via remote-control plugin after upgrading to Grails 2.4 and remove setup from Bootstrap.groovy

		when: "go to the URL normally found from email"
		to ResetPasswordPage, "?t=ForgotPasswordSpec"
		then: "reset password page must be shown"
		at ResetPasswordPage

		when: "an acceptable password is given"
		password << "!#¤%t3stPassword123!"
		waitFor { nextButton.click() }
		waitFor { password2.displayed }
		password2 << "!#¤%t3stPassword123!"
		waitFor { nextButton.click() }
		then: "should log in"
		waitFor {
			at CanvasPage
		}

		when: "logged out"
		navbar.navSettingsLink.click()
		navbar.navLogoutLink.click()
		then: "loginPage visible"
		waitFor {
			at LoginPage
		}

		expect: "logged in with new password"
		login("tester1@streamr.com", "!#¤%t3stPassword123!")

		when: "Changing password (back to original)"
		navbar.navSettingsLink.click()
		navbar.navProfileLink.click()
		waitFor {
			at ProfileEditPage
		}
		changePassword.click()
		waitFor { at ChangePasswordPage }
		currentPassword << "!#¤%t3stPassword123!"
		newPassword << "tester1TESTER1"
		newPasswordAgain << "tester1TESTER1"
		changePassword.click()
		then: "Must go back to profile edit page and show info message"
		waitFor {
			at ProfileEditPage
			$(".alert-info").size()>0
		}
	}
}
