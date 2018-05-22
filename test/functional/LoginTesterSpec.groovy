import geb.driver.CachingDriverFactory
import pages.CanvasPage
import pages.LoginPage
import geb.spock.GebReportingSpec

abstract class LoginTesterSpec extends GebReportingSpec {

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
		at LoginPage
		username = getTesterUsername()
		password = getTesterPassword()
		loginButton.click()
		waitFor {
			at CanvasPage
		}
	}

	abstract String getTesterUsername();
	abstract String getTesterPassword();
}
