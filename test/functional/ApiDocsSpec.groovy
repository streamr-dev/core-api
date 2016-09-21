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

	void "CORE-693 code blocks are not collapsed initially"() {
		to ApiDocsPage
		waitFor { $(".CodeMirror-lines").size() > 0 }

		expect: "all codemirror lines to have a height greater than zero"
		waitFor { js.exec('return Math.min.apply(null, $(".CodeMirror-lines").map(function(i,o) { return $(o).height(); }))') > 0 }
	}

}
