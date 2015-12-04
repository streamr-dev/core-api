import geb.spock.GebReportingSpec

import org.openqa.selenium.Cookie

import spock.lang.Shared
import core.pages.*

class RememberMeSpec extends GebReportingSpec {
	
	@Shared cookieName = "streamr_remember_me"
	@Shared cookieValue = "initial"
	
	// NB! These tests must be executed in order
	
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
		at CanvasPage
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
	
	def "the profile page still needs logging in if the user has logged in to the app with a cookie"(){
		Cookie cookie = new Cookie(cookieName, cookieValue)
		driver.manage().addCookie(cookie)
		when: "go to the profileEditPage"
		to CanvasPage
		navbar.navSettingsLink.click()
		navbar.navProfileLink.click()
		then: "require logging in"
		at LoginPage
	}
	
	def "once the user has logged in to the profile page, a new logging isn't required"(){
		Cookie cookie = new Cookie(cookieName, cookieValue)
		driver.manage().addCookie(cookie)
		when: "first try go to the profileEditPage"
		to CanvasPage
		navbar.navSettingsLink.click()
		navbar.navProfileLink.click()
		then: "require logging in"
		at LoginPage
		when: "log in"
		username = "tester1@streamr.com"
		password = "tester1TESTER1"
		loginButton.click()
		then: "go to the profileEditPage"
		at ProfileEditPage
		when: "go to the canvasPage and back"
		to CanvasPage
		navbar.navSettingsLink.click()
		navbar.navProfileLink.click()
		then: "go straight to the profileEditPage"
		at ProfileEditPage
	}
	
	
}
		