import geb.spock.GebReportingSpec
import core.pages.*

class ForgotPasswordSpec extends GebReportingSpec {
	
	def "go to forgotPasswordPage"(){
		to LoginPage
		
		when: "Clicked 'Forgot password'"
			$(".forgot").click()
		then: "ForgotPasswordPage opened"
			waitFor {
				at ForgotPasswordPage
			}
	}
	
	def "password reset flow"() {
		// TODO: add setup via remote-control plugin after upgrading to Grails 2.4 and remove setup from Bootstrap.groovy
			
		when: "go to the URL normally found from email"
			go "register/resetPassword?t=ForgotPasswordSpec"
		then: "reset password page must be shown"
			at ResetPasswordPage
			
		when: "a weak password is given"
			password << "weakPassword"
			password2 << "weakPassword"
			resetButton.click()
		then: "it must not be accepted"
			waitFor {
				at ResetPasswordPage
			}
			
		when: "an acceptable password is given"
			password << "!#¤%t3stPassword123!"
			password2 << "!#¤%t3stPassword123!"
			resetButton.click()
		then: "should log in"
			waitFor(10) {
				at CanvasPage
			}
		
		when: "logged out"
			navbar.navSettingsLink.click()
			navbar.navLogoutLink.click()
		then: "loginPage visible"
			waitFor {
				at LoginPage
			}
			
		when: "logged in with new password"
			username = "tester1@streamr.com"
			password = "!#¤%t3stPassword123!"
			loginButton.click()
		then: "should log in normally"
			waitFor {
				at CanvasPage
			}
			
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
