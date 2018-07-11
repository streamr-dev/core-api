package mixins

import pages.CanvasPage
import pages.RegisterPage
import pages.SignUpPage

/**
 * Handle registering a new user
 */
trait RegisterMixin {
	def registerUser(String emailAddress, String pwd, String fullName = "Test Tester", String tz = "Europe/Zurich") {
		def code = emailAddress.replaceAll("@", "_")

		when: "requested to get the invitation"
		to SignUpPage
		email = emailAddress
		nextButton.click()
		then: "the invitation is told to be sent"
		waitFor {
			signUpOk.displayed
		}
		when: "registered"
		to RegisterPage, "?invite=" + code
		name = fullName
		nextButton.click()
		waitFor { password.displayed }
		password = pwd
		nextButton.click()
		waitFor { password2.displayed }
		password2 = pwd
		nextButton.click()
		waitFor { timezone.displayed }
		timezone << tz
		timezoneFirstResult.click()
		nextButton.click()
		waitFor {
			agreeCheckbox.displayed
			agreeCheckbox.click()
		}
		nextButton.click()

		then: "go to canvas page"
		at CanvasPage
	}
}
