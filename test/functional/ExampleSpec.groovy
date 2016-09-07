import pages.*
import spock.lang.*
import core.LoginTester2Spec
import core.mixins.CanvasMixin

@Mixin(CanvasMixin)
// Examples are commented out
// https://github.com/streamr-dev/unifina-core/commit/9ba0a477358336750546f1350578d1272c59467c
@Ignore
class ExampleSpec extends LoginTester2Spec {

	def "examples tabs must be shown in the load browser"() {
		when: "load button is clicked"
			loadButton.click()
		then: "load browser must appear"
			waitFor {
				$('#archiveLoadBrowser table')
			}
		
		when: "examples tab is clicked"
			$("ul li > a", text:"Examples").click()
		then: "examples must appear"
			waitFor {
				$('#examplesLoadBrowser table td', text:"ExampleSpec")
			}
			
		when: "example is selected"
			$('#examplesLoadBrowser table td', text:"ExampleSpec").click()
		then: "selected signalpath must load"
			waitFor {
				$("#module_2")
			}
	}
	
	def "examples link in help nav must show load browser"() {
		when: "help link is clicked"
			$("#navHelpLink").click()
		then: "examples option must be shown in help menu"
			waitFor {
				$('#navExamplesLink').displayed
			}
			
		when: "examples link is clicked"
			$("#navExamplesLink").click()
		then: "examples load browser must be displayed"
			waitFor {
				$('#examplesLoadBrowser table td', text:"ExampleSpec")
			}
	}
}
