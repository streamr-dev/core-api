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
		$("ul.jd_menu li",0).click()
		$("ul.jd_menu li",0).find("ul li", text:"Create").click()

		then:
		at UserCreatePage

		when: "data is given and the user is created"
		username = "user-edit-admin-spec@streamr.com"
		name = "Test"
		password = "user-edit-admin-spec"
		timezone = "Europe/Helsinki"

		roleTab.click()
		waitFor {
			roleUser.displayed
		}
		roleUser.click()

		feedTab.click()
		waitFor {
			feedUserStream.displayed
		}
		feedUserStream.click()

		modulePackageTab.click()
		waitFor {
			modulePackageCore.displayed
		}
		modulePackageCore.click()

		create.click()

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

	def "the user can be edited"() {
		when: "search for and go to the user page"
		username = "user-edit-admin-spec@streamr.com"
		searchButton.click()
		at UserSearchResultPage
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
		searchResult.size() == 1
		searchResult.text() == "user-edit-admin-spec2@streamr.com"
	}

	def "the user can be removed"() {
		when: "search for and go to the user page"
		username = "user-edit-admin-spec2@streamr.com"
		searchButton.click()
		at UserSearchResultPage
		searchResult.click()

		then: "go to the user edit page"
		at UserEditPage

		when: "click to delete the user"
		deleteButton.click()

		then:
		waitFor {
			deleteConfirmButton.displayed
		}

		when: "confirmed"
		deleteConfirmButton.click()

		then:
		at UserSearchPage
	}
}



