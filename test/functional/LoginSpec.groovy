import pages.CanvasPage
import pages.LoginPage
import geb.spock.GebReportingSpec

class LoginSpec extends GebReportingSpec {

	def "cannot log in with empty form"() {
		when: "just clicked to log in"
		to LoginPage
		loginButton.click()
		then: "should not go forward"
		waitFor {
			at LoginPage
			$("p.login-failed-message").displayed
		}
	}

	def "cannot log in with false information"() {
		when: "given false username and password"
		to LoginPage
		username = "falseUserName"
		password = "falsePassword"
		loginButton.click()
		then: "should not go forward"
		waitFor {
			at LoginPage
			$("p.login-failed-message").displayed
		}
	}

	def "basic login works"(){
		when: "logged in"
		to LoginPage
		username = "tester1@streamr.com"
		password = "tester1TESTER1"
		loginButton.click()
		then:
		waitFor {
			at CanvasPage
		}
	}
}
