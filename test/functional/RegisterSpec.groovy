
import core.pages.*
import spock.lang.*
import geb.spock.GebReportingSpec

class RegisterSpec extends GebReportingSpec {
	
        def emailAddress = "testingemail@streamr.com"
        def code = emailAddress.replaceAll("@", "_")
        // Just a random password
        def pwd = "Aymaw4HV"
        
        def "requesting the invitation token goes correctly"() {
            when: "requested to get the invitation"
                to SignUpPage
                // Not a real email
                email = emailAddress
                signUpButton.click()
            then: "the invitation is told to be sent"
                waitFor {
                    signUpOk.displayed
                }
        }
        
        def "registering can now be done correctly"() {           
            when: "registered"
                to RegisterPage, "?invite="+ code
                name = "Test Tester"
                password = pwd
                password2 = pwd
                agreeCheckbox.click()
                loginButton.click()
                
            then: "go to canvas page"   
                at CanvasPage
        }
}
