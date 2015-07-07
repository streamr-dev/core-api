import core.pages.ChangePasswordPage;
import core.pages.ProfileEditPage;
import geb.spock.GebReportingSpec
import spock.lang.*
import pages.*

public class ProfileEditSpec extends LoginTester1Spec {

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
		currentPassword << "tester1"
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
		waitFor { at BuildPage }
		
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
		newPassword << "tester1"
		newPasswordAgain << "tester1"
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
		password = "tester1"
		loginButton.click()
		then: "logged in normally"
		waitFor { at BuildPage }
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
		$(".text-danger li", text: "Password length must be at least 8 characters").displayed
		
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
		
		then: "same page with text 'Password not Changed'"
		waitFor { at ChangePasswordPage }
		$(".alert", text:"Password not changed!").displayed
		
//		Password not changed
		
		when: "click to Log Out"
		$("#navSettingsLink").click()
		$("#navLogoutLink").click()
		then: "logged out"
		waitFor { at LoginPage }
		
		when: "logged back with original password"
		username = "tester1@unifina.com"
		password = "KASMoney!Machine1"
		loginButton.click()
		then: "logged in normally"
		waitFor { at BuildPage }
	}
	
	def "settings can be changed permanently"(){
		when: "profile edit page is clicked to open"
		$("#navSettingsLink").click()
		$("#navProfileLink").click()

		then: "must go to profile edit page"
		waitFor { at ProfileEditPage }
		
		when: "settings changed"
		def defaultCommission = $("div.form-group label", text:"Default Commission").closest("div").find("div.input-group").find("input")
		defaultCommission.firstElement().clear()
		defaultCommission << "3.0"
		def defaultLatency = $("div.form-group label", text:"Default Latency").closest("div").find("div.input-group").find("input")
		defaultLatency.firstElement().clear()
		defaultLatency << "1"
		def defaultPortfolioName = $("div.form-group label", text:"Default Portfolio Name").closest("div").find("input")
		defaultPortfolioName.firstElement().clear()
		defaultPortfolioName << "DEFAULT1"
		$("#submit").click()
		
		then: "fields have correct values"
		$("div.alert", text:"Profile updated.").displayed
		$("div.form-group label", text:"Default Commission").closest("div").find("div.input-group").find("input").value() == "3.0"
		$("div.form-group label", text:"Default Latency").closest("div").find("div.input-group").find("input").value() == "1"
		$("div.form-group label", text:"Default Portfolio Name").closest("div").find("input").value() == "DEFAULT1"
		
		when: "click to Log Out"
		$("#navSettingsLink").click()
		$("#navLogoutLink").click()
		
		then: "logged out"
		waitFor { at LoginPage }
			
		when: "log in"
		username = "tester1@unifina.com"
		password = "KASMoney!Machine1"
		loginButton.click()
		
		then: "logged in"
		waitFor { at BuildPage }
		
		when: "go to profile edit page"
		$("#navSettingsLink").click()
		$("#navProfileLink").click()

		then: "profile edit page opened with correct values"
		waitFor { at ProfileEditPage }
		$("div.form-group label", text:"Default Commission").closest("div").find("div.input-group").find("input").value() == "3.0"
		$("div.form-group label", text:"Default Latency").closest("div").find("div.input-group").find("input").value() == "1"
		$("div.form-group label", text:"Default Portfolio Name").closest("div").find("input").value() == "DEFAULT1"
		
		when: "settings changed back"
		def defaultCommission2 = $("div.form-group label", text:"Default Commission").closest("div").find("div.input-group").find("input")
		defaultCommission2.firstElement().clear()
		defaultCommission2 << "1.8"
		def defaultLatency2 = $("div.form-group label", text:"Default Latency").closest("div").find("div.input-group").find("input")
		defaultLatency2.firstElement().clear()
		defaultLatency2 << "0"
		def defaultPortfolioName3 = $("div.form-group label", text:"Default Portfolio Name").closest("div").find("input")
		defaultPortfolioName3.firstElement().clear()
		defaultPortfolioName3 << "DEFAULT"
		$("#submit").click()
		
		then: "fields have original values"
		$("div.alert", text:"Profile updated.").displayed
		$("div.form-group label", text:"Default Commission").closest("div").find("div.input-group").find("input").value() == "1.8"
		$("div.form-group label", text:"Default Latency").closest("div").find("div.input-group").find("input").value() == "0"
		$("div.form-group label", text:"Default Portfolio Name").closest("div").find("input").value() == "DEFAULT"
		}
		
		
		
	}



