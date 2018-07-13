import geb.spock.GebReportingSpec
import mixins.LoginMixin
import pages.DashboardListPage
import pages.LoginPage

class LoginRedirectSpec extends GebReportingSpec implements LoginMixin {
	def setup() {
		logout()
	}

	def "test redirect after login"() {
		when: "trying to access secured page by url"
			go DashboardListPage.url
		then: "must redirect to login page"
			waitFor { at LoginPage }

		when: "logged in"
			tryLogin(LoginTester1Spec.testerUsername, LoginTester1Spec.testerPassword)
		then: "must go to requested page"
			waitFor {
				at DashboardListPage
			}
	}

	def "redirects to redirect parameter value after successful login"() {
		when:
			to LoginPage, "?redirect=https%3A%2F%2Fmarketplace.streamr.com%2Ffoobar"
		then: "must redirect to login page"
			waitFor {
				at LoginPage
			}
		when: "login"
			tryLogin(LoginTester1Spec.testerUsername, LoginTester1Spec.testerPassword)

		then: "redirects browser to url defined by redirect parameter"
			waitFor {
				currentUrl.equals("https://marketplace.streamr.com/foobar")
			}
	}
}
