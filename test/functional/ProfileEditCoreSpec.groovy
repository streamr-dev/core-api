import geb.spock.GebReportingSpec
import mixins.LoginMixin
import mixins.RegisterMixin
import pages.ChangePasswordPage
import pages.LoginPage
import pages.ProfileEditPage
import spock.lang.Shared

class ProfileEditCoreSpec extends GebReportingSpec implements LoginMixin, RegisterMixin {

	@Shared
	def emailAddress = "testingemail${System.currentTimeMillis()}@streamr.com"
	@Shared
	def pwd = "Aymaw4HVa(dB42"
	@Shared
	def pwd2 = "!#¤%testPassword123!?"

	def setupSpec() {
		registerUser(emailAddress, pwd)
	}

	// Delete the user
	def cleanupSpec() {
		removeUser(emailAddress)
	}

	def "changing password works correctly"() {
		setup:
		login(emailAddress, pwd)

		when: "profile edit page is clicked to open"
		navbar.navSettingsLink.click()
		navbar.navProfileLink.click()
		then: "must go to profile edit page"
		waitFor { at ProfileEditPage }

		// Password changed from original to another

		when: "password is changed"
		changePasswordButton.click()
		waitFor { at ChangePasswordPage }
		currentPassword << pwd
		newPassword << pwd2
		newPasswordAgain << pwd2
		changePasswordButton.click()

		then: "profile page must open with text 'Password Changed'"
		waitFor { at ProfileEditPage }
		alert.find { it.text() == "Password changed!" }.displayed

		when: "click to Log Out"
		navbar.navSettingsLink.click()
		navbar.navLogoutLink.click()
		then: "logged out"
		waitFor { at LoginPage }

		expect: "logged back in with the new password"
		login(emailAddress, pwd2)

		// Password changed back to original

		when: "profile edit page is clicked to open"
		navbar.navSettingsLink.click()
		navbar.navProfileLink.click()
		then: "must go to profile edit page"
		waitFor { at ProfileEditPage }

		when: "password is changed"
		changePasswordButton.click()
		waitFor { at ChangePasswordPage }
		currentPassword << pwd2
		newPassword << pwd
		newPasswordAgain << pwd
		changePasswordButton.click()

		then: "profile page must open with text 'Password Changed'"
		waitFor { at ProfileEditPage }
		$(".alert", text: "Password changed!").displayed

		when: "click to Log Out"
		navbar.navSettingsLink.click()
		navbar.navLogoutLink.click()
		then: "logged out"
		waitFor { at LoginPage }

		expect: "logged back in with the 'new' password"
		login(emailAddress, pwd)
	}

	def "password cannot be changed to an invalid one"() {
		setup:
		login(emailAddress, pwd)

		// Wrong Current Password and too short New Password
		when: "profile edit page is clicked to open"
		navbar.navSettingsLink.click()
		navbar.navProfileLink.click()

		then: "must go to profile edit page"
		waitFor { at ProfileEditPage }

		when: "old password typed wrong"
		changePasswordButton.click()
		waitFor { at ChangePasswordPage }
		currentPassword << "INCORRECTPASSWORD"
		newPassword << "shortPW"
		newPasswordAgain << "shortPW"
		changePasswordButton.click()

		then: "same page with text 'Password not Changed'"
		waitFor { at ChangePasswordPage }
		alert.find { it.text() == "Password not changed!" }.displayed
		error.find { it.text() == "Incorrect password!" }.displayed
		error.find { it.text() == "Password length must be at least 8 characters." }.displayed

		// Correct Current Password, New Password without numbers or special characters
		when: "profile edit page is clicked to open"
		navbar.navSettingsLink.click()
		navbar.navProfileLink.click()

		then: "must go to profile edit page"
		waitFor { at ProfileEditPage }

		when: "old password typed right"
		changePasswordButton.click()
		waitFor { at ChangePasswordPage }
		currentPassword << pwd
		newPassword << "nonumberspwd"
		newPasswordAgain << "DIFFERENTnonumberspwd"
		changePasswordButton.click()

		then: "same page with text 'Password not changed'"
		waitFor { at ChangePasswordPage }
		alert.find { it.text() == "Password not changed!" }.displayed

		// Password not changed

		when: "click to Log Out"
		navbar.navSettingsLink.click()
		navbar.navLogoutLink.click()
		then: "logged out"
		waitFor { at LoginPage }

		expect: "logged back with original password"
		login(emailAddress, pwd)
	}
}



