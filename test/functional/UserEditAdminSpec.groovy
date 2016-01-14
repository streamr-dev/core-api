import core.LoginTesterAdminSpec
import core.pages.UserCreatePage
import core.pages.UserEditPage
import core.pages.UserSearchPage
import core.pages.UserSearchResultPage

public class UserEditAdminSpec extends LoginTesterAdminSpec {

	// The order of the tests is requisite

	def setup(){
		to UserSearchPage
	}

	def "user creation works correctly"(){
		when: "click to user create page"
		$("ul.jd_menu li",0).find("ul li a", text:"Create").click()

		then:
		at UserCreatePage

		when: "data is given and the user is created"
		username = "user-edit-admin-spec@streamr.com"
		name = "Test"
		password = "user-edit-admin-spec"
		timezone = "Europe/Helsinki"

		roleUser.click()
		feedUserStream.click()
		modulePackageCore.click()

		createButton.click()

		then:
		at UserSearchPage
	}

	def "the just created user can be searched"(){
		when: "search for the username"
		username = "user-edit-admin-spec@streamr.com"
		searchButton.click()

		then: "the user is found"
		at UserSearchResultPage
		searchResult.size() == 1
		searchResult.text() == "user-edit-admin-spec@streamr.com"
	}

	def "the user password can be changed"() {
		when: "search for and go to the user page"
		username = "user-edit-admin-spec@streamr.com"
		searchButton.click()
		at UserSearchResultPage
		waitFor {
			searchResult.displayed
		}
		searchResult.click()

		then: "go to the user edit page"
		at UserEditPage

		when: "new password typed and saved"
		password = "test-pwd"
		saveButton.click()
		then: "the user saves"
		waitFor {
			at UserSearchPage
		}
		when: "logged out"
		navSettingsLink.click()
		navLogoutLink.click()
		then: "go to loginPage"
		waitFor {
			at LoginPage
		}
		when: "username and pwd typed"
		username = "user-edit-admin-spec@streamr.com"
		password = "test-pwd"
		login.click()
		then: "tester can log in with the new pwd"
		waitFor {
			at CanvasPage
		}

	}

	def "the user can be edited"() {
		when: "search for and go to the user page"
		username = "user-edit-admin-spec@streamr.com"
		searchButton.click()
		at UserSearchResultPage
		waitFor {
			searchResult.displayed
		}
		searchResult.click()

		then: "go to the user edit page"
		at UserEditPage

		when: "change username and save"
		username = "user-edit-admin-spec2@streamr.com"
		saveButton.click()

		then:
		at UserEditPage

		when: "go to the user search page and search for the user"
		to UserSearchPage
		username = "user-edit-admin-spec2@streamr.com"
		searchButton.click()

		then: "the user is found"
		at UserSearchResultPage
		waitFor {
			searchResult.size() == 1
			searchResult.text() == "user-edit-admin-spec2@streamr.com"
		}
	}

	def "the user can be removed"() {
		when: "search for and go to the user page"
		username = "user-edit-admin-spec2@streamr.com"
		searchButton.click()
		at UserSearchResultPage
		searchResult.click()

		then: "go to the user edit page"
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



