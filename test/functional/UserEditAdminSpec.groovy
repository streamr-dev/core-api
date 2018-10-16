import mixins.LoginMixin
import pages.*
import spock.lang.Shared
import spock.lang.Stepwise

// The order of the tests is important
@Stepwise
class UserEditAdminSpec extends LoginTesterAdminSpec implements LoginMixin {

	@Shared specUserName = "me-edit-admin-spec@streamr.com"
	@Shared specUserPwd = "me-edit-admin-spec"

	private void goChangeUserPassword(String userNameToChange, String newPassword) {
		to UserSearchPage
		username = userNameToChange
		searchButton.click()

		waitFor { at UserSearchResultPage }
		waitFor {
			searchResult.displayed
		}
		searchResult.click()

		waitFor { at UserEditPage }
		password = newPassword
		saveButton.click()
		waitFor {
			at UserEditPage
		}
	}

	def setup(){
		to UserSearchPage
	}

	def "user creation works correctly"(){
		when: "click to me create page"
		$("ul.jd_menu li",0).find("ul li a", text:"Create").click()

		then:
		at UserCreatePage

		when: "data is given and the me is created"
		username = specUserName
		name = "Test"
		password = specUserPwd
		timezone = "Europe/Helsinki"

		roleUser.click()
		feedUserStream.click()
		modulePackageCore.click()

		createButton.click()

		then:
		waitFor { at UserSearchPage }
	}

	def "the just created user can be searched"(){
		when: "search for the username"
		username = specUserName
		searchButton.click()

		then: "the me is found"
		at UserSearchResultPage
		searchResult.size() == 1
		searchResult.text() == specUserName
	}

	def "the user password can be changed"() {
		goChangeUserPassword(specUserName, "test-pwd")

		when: "logout and relogin with new password"
			logoutLink.click()
			login(specUserName, "test-pwd")
		then: "successful login"
			waitFor { at CanvasPage }

		when: "log out"
			navbar.navSettingsLink.click()
			navbar.navLogoutLink.click()
			login(testerUsername, testerPassword)
		then: "successful login"
			waitFor { at CanvasPage }
			// Change back the me password
			goChangeUserPassword(specUserName, specUserPwd)
	}

	def "the user can be edited"() {
		when: "search for and go to the me page"
		username = specUserName
		searchButton.click()
		at UserSearchResultPage
		waitFor {
			searchResult.displayed
		}
		searchResult.click()

		then: "go to the me edit page"
		at UserEditPage

		when: "change username and save"
		username = "me-edit-admin-spec2@streamr.com"
		saveButton.click()

		then:
		at UserEditPage

		when: "go to the me search page and search for the me"
		to UserSearchPage
		username = "me-edit-admin-spec2@streamr.com"
		searchButton.click()

		then: "the me is found"
		at UserSearchResultPage
		waitFor {
			searchResult.size() == 1
			searchResult.text() == "me-edit-admin-spec2@streamr.com"
		}
	}

	def "the user can be removed"() {
		when: "search for and go to the me page"
		username = "me-edit-admin-spec2@streamr.com"
		searchButton.click()
		at UserSearchResultPage
		searchResult.click()

		then: "go to the me edit page"
		at UserEditPage

		when: "when clicked to delete and then cancel"
		withConfirm(false) {
			deleteButton.click()
		}

		then:
		at UserEditPage

		when: "when clicked to delete and then OK"
		withConfirm(true) {
			deleteButton.click()
		}

		then:
		at UserSearchPage
	}
}



