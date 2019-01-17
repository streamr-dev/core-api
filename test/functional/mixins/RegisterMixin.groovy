package mixins

import pages.CanvasPage
import pages.LoginPage
import pages.RegisterPage
import pages.SignUpPage
import pages.UserEditPage
import pages.UserSearchPage
import pages.UserSearchResultPage
import com.unifina.domain.security.SignupInvite
import grails.plugin.remotecontrol.RemoteControl

/**
 * Handle registering a new user
 */
trait RegisterMixin {
	def registerUser(String emailAddress, String pwd, String fullName = "Test Tester") {
		def remote = new RemoteControl()

		when: "requested to get the invitation"
		to SignUpPage
		email = emailAddress
		nextButton.click()
		then: "the invitation is told to be sent"
		waitFor {
			signUpOk.displayed
		}
		when: "registered"
		def code = remote {
			SignupInvite.findByUsername(emailAddress).code
		}
		to RegisterPage, "?invite=" + code
		name = fullName
		nextButton.click()
		waitFor { password.displayed }
		password = pwd
		nextButton.click()
		waitFor { password2.displayed }
		password2 = pwd
		nextButton.click()
		waitFor {
			agreeCheckbox.displayed
			agreeCheckbox.click()
		}
		nextButton.click()

		then: "go to canvas page"
		at CanvasPage
	}

	def removeUser(emailAddress) {
		setup: "login"
		login(LoginTesterAdminSpec.testerUsername, LoginTesterAdminSpec.testerPassword)

		when: "search for the user and click it"
		to UserSearchPage
		assert username.displayed
		username = emailAddress
		searchButton.click()
		waitFor {
			at UserSearchResultPage
		}
		searchResult.click()

		then: "go to user edit page"
		at UserEditPage

		when: "click to delete"
		withConfirm(true) {
			deleteButton.click()
		}

		then: "goes to search page"
		at UserSearchPage

		when:
		$("#loginLinkContainer a").click()
		then:
		at LoginPage
	}
}
