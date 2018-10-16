import geb.spock.GebReportingSpec
import mixins.LoginMixin
import pages.*
import spock.lang.Shared
import grails.plugin.remotecontrol.RemoteControl
import com.unifina.domain.security.RegistrationCode

class ForgotPasswordSpec extends GebReportingSpec implements LoginMixin {

	@Shared newPwd = "!#¤%t3stPassword123!"

	def createToken(username) {
		return (new RemoteControl()) {
			def registrationCode = new RegistrationCode(username: username)
			registrationCode.save()
			registrationCode.token
		}
	}

	def "go to forgotPasswordPage"(){
		setup:
		to LoginPage
		username = "random@me.name"
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
		then: "the password field must be enabled"
		at ResetPasswordPage
		waitFor {
			password.displayed
			!password.@disabled
		}

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
		def token = createToken(LoginTester1Spec.testerUsername)

		when: "go to the URL normally found from email"
		to ResetPasswordPage, "?t=${token}"
		then: "reset password page must be shown"
		at ResetPasswordPage

		when: "an acceptable password is given"
		password << newPwd
		waitFor { nextButton.click() }
		waitFor { password2.displayed }
		password2 << newPwd
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
		login(LoginTester1Spec.testerUsername, newPwd)

		when: "Changing password (back to original)"
		to ProfileEditPage
		waitFor {
			changePasswordButton.displayed
			changePasswordButton.click()
		}
		waitFor { at ChangePasswordPage }
		currentPassword << newPwd
		newPassword << LoginTester1Spec.testerPassword
		newPasswordAgain << LoginTester1Spec.testerPassword
		changePasswordButton.click()
		then: "Must go back to profile edit page and show info message"
		waitFor {
			at ProfileEditPage
			$(".alert-info").size() > 0
		}
	}
}
