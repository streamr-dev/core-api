import mixins.LoginMixin
import pages.LoginPage
import geb.spock.GebReportingSpec

class LoginSpec extends GebReportingSpec implements LoginMixin {

	def "cannot log in with empty form"() {
		when: "just clicked to log in"
		to LoginPage
		nextButton.click()
		then: "should not go onMessage"
		waitFor {
			at LoginPage
			error.displayed
		}
	}

	def "cannot log in with false credentials"() {
		when: "given false username and password"
		to LoginPage
		tryLogin("false@user.name", "falsePassword")
		then: "should not go onMessage"
		waitFor {
			at LoginPage
			error.displayed
		}
	}

	def "basic login works"(){
		expect: "logged in"
		to LoginPage
		loginTester1()
	}
}
