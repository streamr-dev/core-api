import org.openqa.selenium.Keys

import core.LoginTester1Spec
import core.mixins.CanvasMixin


class SearchModuleSpec extends LoginTester1Spec {

	def setupSpec() {
		// @Mixin is buggy, don't use it
		SearchModuleSpec.metaClass.mixin(CanvasMixin)
	}
	
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
		then: "the first result begins with the search term"
			waitFor {
				$(".streamr-search-suggestion-name", 0).text() == "IfThenElse"
			}
			
	}
	
}