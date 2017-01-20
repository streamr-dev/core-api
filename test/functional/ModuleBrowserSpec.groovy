import core.LoginTester1Spec
import core.pages.ModuleBrowserPage
import org.openqa.selenium.Dimension

class ModuleBrowserSpec extends LoginTester1Spec {

	def setup() {
		// necessary to make sure table of contents column is shown
		if (driver.manage().window().size.width < 1000) {
			driver.manage().window().size = new Dimension(1000, 800)
		}

		when:
		$("#navHelpLink").click()
		then:
		waitFor { $("#navModuleReferenceLink").displayed }

		when:
		$("#navModuleReferenceLink").click()
		then:
		waitFor { at ModuleBrowserPage }
	}

	void "module browser can be opened via help menu"() {
		when: "click on last top-level title of user guide"
		def link = tableOfContents.children().last().children("a")

		// Wait for JavaScript logic to finish
		waitFor { !link.text().empty }
		link.click()

		then: "the corresponding header should be visible"
		$("h2", text:link.text()).displayed
	}

	void "module loads when title clicked"() {
		setup:
		def link = tableOfContents.children().last().children("a")
		waitFor { !link.text().empty }
		link.click()
		waitFor {
			$("span", text:'CreateStream').displayed
		}

		when: "title clicked"
		$(".panel .panel-heading span", text:'CreateStream').click()

		then: "help is shown"
		waitFor {
			$("div.help-text p", text:'Create a new stream.').displayed
		}
	}

}
