import core.LoginTester1Spec
import core.pages.ModuleBrowserPage

class ModuleBrowserSpec extends LoginTester1Spec {

	void "module browser table of contents works"() {
		when:
		$("#navHelpLink").click()
		then:
		waitFor { $("#navModuleReferenceLink").displayed }

		when:
		$("#navModuleReferenceLink").click()
		then:
		waitFor { at ModuleBrowserPage }

		when: "click on last top-level title"
		def link = tableOfContents.children().last().children("a")
		def text = link.text()
		link.click()
		then: "the corresponding header should be visible"
		$("h2", text: text).displayed
	}

}
