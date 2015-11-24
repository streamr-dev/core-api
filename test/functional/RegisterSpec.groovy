
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
            // The environment must be TEST so the SignUpCodeService creates a previsible invitation token
            expect:
            assert Environment.current == Environment.TEST
        }

        // Delete the user
        def cleanupSpec() {
            when: "login"
                to LoginPage
                username = "tester-admin@streamr.com"
                password = "tester-adminTESTER-ADMIN"
                loginButton.click()
            then:
                at CanvasPage

            when: "search for the user and click it"
                navbar.navAdminLink.click()
                $("li a", text:"Users").click()
                waitFor {
                    at UserSearchPage
                }
                assert username.displayed
                username = emailAddress
                searchButton.click()
                waitFor {
                    at UserSearchResultPage
                }
                searchResult.click()

            then: "go to user edit page"
                waitFor {
                    at UserEditPage
                }

            when: "click to delete"
                deleteButton.click()
            then: "asks for confirmation"
                assert confirmationBox.displayed

            when: "confirms"
                deleteConfirmButton.click()
            then: "go back to user search page"
                waitFor {
                    at UserSearchPage
                }
                // A notification is blocking the logout button, the easiest way to get rid of it is to reload the page
                to UserSearchPage

            when:
                $("#loginLinkContainer a").click()
            then:
                at LoginPage
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
