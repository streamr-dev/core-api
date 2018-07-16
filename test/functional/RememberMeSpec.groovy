import mixins.LoginMixin
import pages.CanvasPage
import pages.LoginPage
import pages.ProfileEditPage
import geb.spock.GebReportingSpec
import spock.lang.Shared
import spock.lang.Stepwise

@Stepwise
class RememberMeSpec extends GebReportingSpec implements LoginMixin {

	@Shared sessionCookieName = "JSESSIONID"
	@Shared rememberMeCookieName = "streamr_remember_me"
	@Shared rememberMeCookieValue = "initial"

	// NB! These tests must be executed in order

	def setup() {
		driver.manage().deleteAllCookies()
		logout()
	}

	def loginAndSaveCookie() {
		login(LoginTester1Spec.testerUsername, LoginTester1Spec.testerPassword, true)
		rememberMeCookieValue = driver.manage().getCookieNamed(rememberMeCookieName).getValue()
		// Remove the session cookie
		driver.manage().deleteCookie(driver.manage().getCookieNamed(sessionCookieName))
	}

	def "rememberMe works"(){
		setup: "logged in with remember me"
		loginAndSaveCookie()

		when: "go to canvas page"
		go CanvasPage.url
		then: "the browser still remembers me"
		waitFor { at CanvasPage }
	}

	def "the profile page still needs logging in if the user has logged in to the app with a cookie"(){
		setup:
		loginAndSaveCookie()

		when: "go to the profileEditPage"
		to CanvasPage
		go ProfileEditPage.url
		then: "require logging in"
		waitFor {
			at LoginPage
		}
	}

	def "once the user has logged in to the profile page, a new logging isn't required"(){
		setup:
		loginAndSaveCookie()

		when: "first try go to the profileEditPage"
		to CanvasPage
		go ProfileEditPage.url
		then: "require logging in"
		waitFor {
			at LoginPage
		}
		when: "log in"
		tryLogin(LoginTester1Spec.testerUsername, LoginTester1Spec.testerPassword)
		then: "go to the profileEditPage"
		at ProfileEditPage
		when: "go to the canvasPage and back"
		to CanvasPage
		to ProfileEditPage
		then: "go straight to the profileEditPage"
		at ProfileEditPage
	}


}
