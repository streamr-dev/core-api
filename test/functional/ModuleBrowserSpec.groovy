import core.LoginTester1Spec
import core.pages.ModuleBrowserPage

class ModuleBrowserSpec extends LoginTester1Spec {

	void "module browser can be opened via help menu"() {
		when:
		$("#navHelpLink").click()
		then:
		waitFor { $("#navModuleReferenceLink").displayed }

		when:
		$("#navModuleReferenceLink").click()
		then:
		waitFor { at ModuleBrowserPage }

		when: "click on last top-level title of user guide"
		def link = tableOfContents.children().last().children("a")

		// Wait for JavaScript logic to finish
		waitFor { !link.text().empty }
		link.click()

		then: "the corresponding header should be visible"
		$("h2", text:link.text()).displayed
	}

}
