import core.LoginTester1Spec
import core.pages.ModuleBrowserPage
import org.openqa.selenium.Dimension

class ModuleBrowserSpec extends LoginTester1Spec {

	void "module browser table of contents works"() {
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
		when: "clicked on 'Module Browser' navigation link"
		navbar.navHelpLink.click()
		def link = navbar.navModuleReferenceLink

		// Wait for JavaScript logic to finish
		waitFor { !link.text().empty }
		def text = link.text()
		link.click()

		then: "breadcrumb contains 'All modules by category'"
		$(".breadcrumb").text() == "All modules by category"
	}

	void "module loads when title clicked"() {
		setup:
		navbar.navHelpLink.click()
		def link = navbar.navModuleReferenceLink
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
