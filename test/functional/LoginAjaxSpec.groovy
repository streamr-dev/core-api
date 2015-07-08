import core.LoginTester1Spec;
import geb.spock.GebReportingSpec
import spock.lang.*
import pages.*

class LoginAjaxSpec extends LoginTester1Spec {
	
	def "ajax login screen"() {
		when: "session expires"
			js.exec("\$.ajax({url: Streamr.createLink('logout','index'), async:false})")
		then: "login form must be displayed"
			waitFor {
				$("#loginForm").displayed
				js.exec("return document.activeElement.id") == "username"
			}
			
		when: "correct credentials are entered and login button is clicked"
			$("#username") << testerUsername
			$("#password") << testerPassword
			$("#loginButton").click()
		then: "login form must disappear"
			waitFor {
				$("#loginForm").size()==0
			}
	}
	
	def "ajax login screen failed login message"() {
		when: "session expires"
			js.exec("\$.ajax({url: Streamr.createLink('logout','index'), async:false})")
		then: "login form must be displayed"
			waitFor {
				$("#loginForm").displayed
				js.exec("return document.activeElement.id") == "username"
			}
			
		when: "invalid credentials are entered and login button is clicked"
			$("#username") << "asdasd"
			$("#password") << "asdasd"
			$("#loginButton").click()
		then: "error message must appear"
			waitFor {
				$("#loginForm .login-failed-message").displayed
			}
	}
	
}
