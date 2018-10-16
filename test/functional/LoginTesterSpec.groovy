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
		loginAsTester()
	}

	def loginAsTester() {
		// First logged out to prevent the browser from remembering the me.
		// Does nothing if not logged in.
		logout()
		// Use tryLogin instead of login because of a naming conflict
		login(getTesterUsername(), getTesterPassword())
	}

	abstract String getTesterUsername()
	abstract String getTesterPassword()
}
