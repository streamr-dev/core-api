import pages.*
import spock.lang.*
import core.pages.CanvasPage

class LoginSpec extends LoginTester1Spec {
	def "should be logged in"() {
		expect:
			at CanvasPage
	}
}
