import geb.spock.GebReportingSpec
import core.pages.*
import spock.lang.*

class LoginRedirectSpec extends GebReportingSpec {
	def "test redirect after login"() {
		when: "trying to access secured page by url"
			go DashboardListPage.url
		then: "must redirect to login page"
			waitFor { at LoginPage }
			
		when: "logged in"
			username = "tester1@streamr.com"
			password = "tester1TESTER1"
			loginButton.click()
		then: "must go to requested page"
			waitFor {
				at DashboardListPage
			}
	}
}
