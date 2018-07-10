import geb.driver.CachingDriverFactory
import mixins.LoginMixin
import pages.CanvasPage
import pages.LoginPage
import geb.spock.GebReportingSpec

abstract class LoginTesterSpec extends GebReportingSpec implements LoginMixin {

	def setupSpec() {
		resetBrowser()
		CachingDriverFactory.clearCacheAndQuitDriver()
	}

	def setup() {
		this.login()
	}

	def login() {
		// First logged out to prevent the browser from remembering the user.
		// Does nothing if not logged in.
		go "logout"
		login(getTesterUsername(), getTesterPassword())
	}

	abstract String getTesterUsername()
	abstract String getTesterPassword()
}
