import mixins.LoginMixin
import mixins.RegisterMixin
import pages.*
import geb.spock.GebReportingSpec
import grails.util.Environment
import spock.lang.Shared
import spock.lang.Stepwise
import com.unifina.service.SignupCodeService
import grails.plugin.remotecontrol.RemoteControl

@Stepwise
class RegisterSpec extends GebReportingSpec implements LoginMixin, RegisterMixin {

	// Not a real email
	@Shared
	def emailAddress = "testingemail${System.currentTimeMillis()}@streamr.com"
	// Just a random password
	@Shared
	def pwd = "Aymaw4HVa(dB42"

	def createSignupCode(username) {
		def remote = new RemoteControl()
		return remote {
			def invite = (new SignupCodeService()).create(username)
			invite.sent = true
			invite.save()
			invite.code
		}
	}

	def setup() {
		logout()
	}

	def setupSpec() {
		// The environment must be TEST so the SignUpCodeService creates a previsible invitation token
		expect:
		assert Environment.current == Environment.TEST
	}

	// Delete the me
	def cleanupSpec() {
		setup: "login"
			login("tester-admin@streamr.com", "tester-adminTESTER-ADMIN")

		when: "search for the me and click it"
			to UserSearchPage
			assert username.displayed
			username = emailAddress
			searchButton.click()
			waitFor {
				at UserSearchResultPage
			}
			searchResult.click()

		then: "go to me edit page"
			at UserEditPage

		when: "click to delete"
			withConfirm(true) {
				deleteButton.click()
			}

		then: "goes to search page"
			at UserSearchPage

		when:
			$("#loginLinkContainer a").click()
		then:
			at LoginPage
	}

	def "cannot request signup token for invalid email"() {
		when: "requested to get the invitation"
			to SignUpPage
			email = "foobar"
			nextButton.click()
		then: "error is shown"
			waitFor {
				at SignUpPage
				error.displayed
			}
	}

	def "register flow works"() {
		expect: "register a new me"
		registerUser(emailAddress, pwd)
	}

	def "cannot register without accepting tos"() {
		when: "registered"
			to RegisterPage, "?invite=${createSignupCode(emailAddress)}"
			name = "Test Tester"
			nextButton.click()
			waitFor { password.displayed }
			password = pwd
			nextButton.click()
			waitFor { password2.displayed }
			password2 = pwd
			nextButton.click()
			waitFor { timezone.displayed }
			timezone << "Europe/Zurich"
			timezoneFirstResult.click()
			nextButton.click()
			waitFor {
				agreeCheckbox.displayed
				nextButton.click()
			}

		then: "show error"
			at RegisterPage
			waitFor { error.displayed }
	}
}
