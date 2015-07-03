import geb.spock.GebReportingSpec
import pages.*
import spock.lang.*
import core.pages.CanvasPage
import core.pages.LoginPage

public class LoginTester1Spec extends GebReportingSpec {
	
	def tester1Username = "tester1@streamr.com"
	def tester1Password = "tester1"
	
	def setup() {
		to LoginPage
		username = tester1Username
		password = tester1Password
		loginButton.click()
		waitFor {
			at CanvasPage
		}
	}
}
