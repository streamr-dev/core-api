import geb.spock.GebReportingSpec
import pages.*
import spock.lang.*

class LoginRedirectSpec extends GebReportingSpec {
	def "test redirect after login"() {
		when: "trying to access secured page by url"
			go BacktestListPage.url
		then: "must redirect to login page"
			waitFor { at LoginPage }
			
		when: "logged in"
			username = "Unifina"
			password = "KASMoney!Machine"
			loginButton.click()
		then: "must go to requestet page"
			waitFor {
				at BacktestListPage
			}
	}
}
