import core.LoginTester1Spec
import core.pages.ApiDocsPage

class ApiDocsSpec extends LoginTester1Spec {

	void "api docs can be opened via help menu"() {
		when:
		$("#navHelpLink").click()
		then:
		waitFor { $("#navApiDocsLink").displayed }

		when:
		$("#navApiDocsLink").click()
		then:
		waitFor { at ApiDocsPage }

		when: "click on last top-level title of user guide"
		def link = tableOfContents.children().last().children("a")
		link.click()
		then: "the corresponding header should be visible"
		$("h1", text:link.text()).displayed
	}

}
