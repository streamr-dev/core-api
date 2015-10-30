
import core.pages.*
import geb.spock.GebReportingSpec

class RegisterSpec extends GebReportingSpec {
	
        def setup() {
            to LoginPage
        }
        
        def "requesting the invitation token"() {
            signUpButton.click()
            at SignUpPage
        }
}
