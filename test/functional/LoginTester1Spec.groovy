import geb.spock.GebReportingSpec
import pages.*
import spock.lang.*
import core.pages.CanvasPage
import core.pages.LoginPage

public class LoginTester1Spec extends GebReportingSpec {
	def setup() {
		to LoginPage
		username = "tester1@streamr.com"
		password = "tester1"
		loginButton.click()
		waitFor {
			at CanvasPage
		}
	}
}
