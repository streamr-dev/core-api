
import core.pages.*
import spock.lang.*
import geb.spock.GebReportingSpec

class RegisterSpec extends GebReportingSpec {
	
        def setup() {
            to SignUpPage
        }
        
        def "requesting the invitation token goes correctly"() {
            when: "requested to get the invitation"
                email = "testingemail@streamr.com"
                signUpButton.click()
            then: "the invitation is told to be sent"
                waitFor {
                    signUpOk.displayed
                }
        }
}
