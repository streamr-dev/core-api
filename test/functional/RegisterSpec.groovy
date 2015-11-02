
import grails.util.Environment
import core.pages.*
import spock.lang.*
import geb.spock.GebReportingSpec

class RegisterSpec extends GebReportingSpec {
        
        // Not a real email
        @Shared
        def emailAddress = "testingemail@streamr.com"
        @Shared
        def code = emailAddress.replaceAll("@", "_")
        // Just a random password
        @Shared
        def pwd = "Aymaw4HV"
        
        def setup() {
            // The environment must be TEST so the SignUpCodeService creates a preisible invitation token
            expect:
            assert Environment.current == Environment.TEST
        }
        
        // Delete the user
        def cleanupSpec() {
            when: "go to user search page"
                to LoginPage
                username = "tester-admin@streamr.com"
                password = "tester-adminTESTER-ADMIN"
                loginButton.click()
            then:
                at CanvasPage
                
            when: "search for the user and click it"
                to UserSearchPage, "?username="+emailAddress
            then: "go to user edit page"
                userList.find("tr", 1).find("td",0).find("a", 0).click()
                at UserEditPage
                
            when: "click to delete"
                deleteButton.click()
            then: "asks for confirmation"
                assert confirmationBox.displayed
                
            when: "confirms"
                deleteConfirmButton.click()
            then: "go back to user search page"
                at UserSearchPage
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
