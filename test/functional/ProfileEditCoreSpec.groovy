import spock.lang.*
import core.LoginTester1Spec
import core.pages.*

public class ProfileEditCoreSpec extends LoginTester1Spec {

	def "changing password works correctly"() {
		when: "profile edit page is clicked to open"
		$("#navSettingsLink").click()
		$("#navProfileLink").click()
		then: "must go to profile edit page"
		waitFor { at ProfileEditPage }
		
//		Password changed from original to another
		
		when: "password is changed"
		changePassword.click()
		waitFor { at ChangePasswordPage }
		currentPassword << "tester1TESTER1"
		newPassword << "!#¤%testPassword123!?"
		newPasswordAgain << "!#¤%testPassword123!?"
		changePassword.click()
		
		then: "profile page must open with text 'Password Changed'"
		waitFor { at ProfileEditPage }
		$(".alert", text:"Password changed!").displayed

		when: "click to Log Out"
		$("#navSettingsLink").click()
		$("#navLogoutLink").click()
		then: "logged out"
		waitFor { at LoginPage }

		when: "logged back in with the new password"
		username = "tester1@streamr.com"
		password = "!#¤%testPassword123!?"
		loginButton.click()
		then: "logged in normally"
		waitFor { at CanvasPage }
		
//		Password changed back to original
		
		when: "profile edit page is clicked to open"
		$("#navSettingsLink").click()
		$("#navProfileLink").click()
		then: "must go to profile edit page"
		waitFor { at ProfileEditPage }
		
		when: "password is changed"
		changePassword.click()
		waitFor { at ChangePasswordPage }
		currentPassword << "!#¤%testPassword123!?"
		newPassword << "tester1TESTER1"
		newPasswordAgain << "tester1TESTER1"
		changePassword.click()
		
		then: "profile page must open with text 'Password Changed'"
		waitFor { at ProfileEditPage }
		$(".alert", text:"Password changed!").displayed

		when: "click to Log Out"
		$("#navSettingsLink").click()
		$("#navLogoutLink").click()
		then: "logged out"
		waitFor { at LoginPage }

		when: "logged back in with the 'new' password"
		username = "tester1@streamr.com"
		password = "tester1TESTER1"
		loginButton.click()
		then: "logged in normally"
		waitFor { at CanvasPage }
	}
	
	def "password cannot be changed to an invalid one"() {
//		Wrong Current Password and too short New Password
		when: "profile edit page is clicked to open"
		$("#navSettingsLink").click()
		$("#navProfileLink").click()
		
		then: "must go to profile edit page"
		waitFor { at ProfileEditPage }
		
		when: "old password typed wrong"
		changePassword.click()
		waitFor { at ChangePasswordPage }
		currentPassword << "INCORRECTPASSWORD"
		newPassword << "shortPW"
		newPasswordAgain << "shortPW"
		changePassword.click()
		
		then: "same page with text 'Password not Changed'"
		waitFor { at ChangePasswordPage }
		$(".alert", text:"Password not changed!").displayed
		$(".text-danger li", text: "Incorrect password!").displayed
		$(".text-danger li", text: "Please use a stronger password!").displayed

//		Correct Current Password, New Password without numbers or special characters
		when: "profile edit page is clicked to open"
		$("#navSettingsLink").click()
		$("#navProfileLink").click()
		
		then: "must go to profile edit page"
		waitFor { at ProfileEditPage }
		
		when: "old password typed right"
		changePassword.click()
		waitFor { at ChangePasswordPage }
		currentPassword << "KASMoney!Machine1"
		newPassword << "nonumberspwd"
		newPasswordAgain << "DIFFERENTnonumberspwd"
		changePassword.click()
		
		then: "same page with text 'Password not changed'"
		waitFor { at ChangePasswordPage }
		$(".alert", text:"Password not changed!").displayed
		
//		Password not changed
		
		when: "click to Log Out"
		$("#navSettingsLink").click()
		$("#navLogoutLink").click()
		then: "logged out"
		waitFor { at LoginPage }
		
		when: "logged back with original password"
		username = "tester1@streamr.com"
		password = "tester1TESTER1"
		loginButton.click()
		then: "logged in normally"
		waitFor { at CanvasPage }
	}	
}



