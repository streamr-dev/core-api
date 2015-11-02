
import grails.util.Environment
import core.pages.*
import spock.lang.*
import geb.spock.GebReportingSpec

class RegisterSpec extends GebReportingSpec {
	
        
        // Not a real email
        def emailAddress = "testingemail2@streamr.com"
        def code = emailAddress.replaceAll("@", "_")
        // Just a random password
        def pwd = "Aymaw4HV"
        
        def setup() {
            // The environment must be TEST so the SignUpCodeService creates a preisible invitation token
            expect:
            assert Environment.current == Environment.TEST
        }
        
        def "the invitation token can be requested correctly"() {
            when: "requested to get the invitation"
                to SignUpPage
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
