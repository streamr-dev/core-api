import geb.spock.GebReportingSpec

import org.openqa.selenium.Cookie

import spock.lang.Shared
import core.pages.CanvasPage
import core.pages.LoginPage

class LoginSpec extends GebReportingSpec {
	
	def "cannot log in with empty form"() {
		when: "just clicked to log in"
		to LoginPage
		loginButton.click()
		then: "should not go forward"
		at LoginPage
		$("p.login-failed-message").displayed
	}
	
	def "cannot log in with false information"() {
		when: "given false username and password"
		to LoginPage
		username = "falseUserName"
		password = "falsePassword"
		loginButton.click()
		then: "should not go forward"
		at LoginPage
		$("p.login-failed-message").displayed
	}
	
	def "login without remember me works"(){
		when: "logged in"
		to LoginPage
		username = "tester1@streamr.com"
		password = "tester1TESTER1"
		loginButton.click()
		then: 
		at CanvasPage
	}
	
	@Shared cookieName = "streamr_remember_me"
	@Shared cookieValue = "initial"
	
	def "log in with checking remember me"(){
		when: "logged in and clicked remember me"
		to LoginPage
		then: "the remember me checkbox is visible"
		rememberMeCheckbox.displayed
		when: "logged in and checked the remember me"
		username = "tester1@streamr.com"
		password = "tester1TESTER1"
		rememberMeCheckbox.click()
		loginButton.click()
		waitFor {
			at CanvasPage
		}
		cookieValue = driver.manage().getCookieNamed(cookieName).getValue()
		then: "at canvaspage" 
		at CanvasPage
	}
	
	def "the browser should remember me"(){
		Cookie cookie = new Cookie(cookieName, cookieValue)
		driver.manage().addCookie(cookie)
		expect: "should be able to go straight to the canvas page"
		to CanvasPage
	}
	
	
}
