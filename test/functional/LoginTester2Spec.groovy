import geb.spock.GebReportingSpec
import pages.*
import spock.lang.*
import core.pages.CanvasPage
import core.pages.LoginPage

public class LoginTester2Spec extends GebReportingSpec {
	
	def testerUsername = "tester2@streamr.com"
	def testerPassword = "tester2"
	
	def setup() {
		to LoginPage
		username = testerUsername
		password = testerPassword
		loginButton.click()
		waitFor {
			at CanvasPage
		}
	}
}
