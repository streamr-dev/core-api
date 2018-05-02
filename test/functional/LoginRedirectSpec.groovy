import geb.spock.GebReportingSpec
import pages.DashboardListPage
import pages.LoginPage

class LoginRedirectSpec extends GebReportingSpec {
	def "test redirect after login"() {
		when: "trying to access secured page by url"
			go DashboardListPage.url
		then: "must redirect to login page"
			waitFor { at LoginPage }

		when: "logged in"
			username = LoginTester1Spec.testerUsername
			password = LoginTester1Spec.testerPassword
			loginButton.click()
		then: "must go to requested page"
			waitFor {
				at DashboardListPage
			}
	}

	def "redirects to redirect parameter value after successful login"() {
		when:
			go LoginPage.url + "?redirect=https%3A%2F%2Fmarketplace.streamr.com%2Ffoobar"
		then: "must redirect to login page"
			waitFor {
				at LoginPage
			}
		when: "login"
			username = LoginTester1Spec.testerUsername
			password = LoginTester1Spec.testerPassword
			loginButton.click()
		then: "redirects browser to url defined by redirect parameter"
			waitFor {
				currentUrl.equals("https://marketplace.streamr.com/foobar")
			}
	}
}
