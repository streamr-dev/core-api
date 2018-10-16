import LoginTester1Spec
import pages.UserGuidePage
import org.openqa.selenium.Dimension

class UserGuideSpec extends LoginTester1Spec {

	void "user guide can be opened via help menu"() {
		// necessary to make sure table of contents column is shown
		if (driver.manage().window().size.width < 1000) {
			driver.manage().window().size = new Dimension(1000, 800)
		}

		when:
		$("#navHelpLink").click()
		then:
		waitFor { $("#navUserGuideLink").displayed }

		when:
		$("#navUserGuideLink").click()
		then:
		waitFor { at UserGuidePage }

		when: "click on last top-level title of me guide"
		def link = tableOfContents.children().last().children("a")
		link.click()
		then: "the corresponding header should be visible"
		$("h1", text:link.text()).displayed
	}

	void "CORE-693 code blocks are not collapsed initially"() {
		to UserGuidePage
		waitFor { $(".CodeMirror-lines").size() > 0 }

		expect: "all codemirror lines to have a height greater than zero"
		waitFor { js.exec('return Math.min.apply(null, $(".CodeMirror-lines").map(function(i,o) { return $(o).height(); }))') > 0 }
	}

}
