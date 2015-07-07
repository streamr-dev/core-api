import geb.spock.GebReportingSpec

import org.openqa.selenium.Keys

import pages.*
import spock.lang.*
import core.mixins.CanvasMixin
import core.pages.LoginPage

@Mixin(CanvasMixin)
class SearchModuleSpec extends LoginTester1Spec {

	
	void "must select the first result with enter"() {
		when: "typed the partial name of a module in the search bar and pressed enter"
			$("#search") << "if"
			$("#search") << Keys.ENTER
		then: "module should appear to the canvas"
			moduleShouldAppearOnCanvas 'IfThenElse'
	}
	
	void "the order of the results should be correct"() {
		when: "typed the partial name of a module"
			$("#search") << "if"
			waitFor {
				$('.tt-suggestion', text:iContains(name))
			}
		then: "the first result begins with the search term"
			waitFor {
				$(".tt-suggestion-name", 0).text() == "IfThenElse"
			}
			
	}
	
}