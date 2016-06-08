import core.LoginTester1Spec
import core.pages.UserGuidePage

class UserGuideSpec extends LoginTester1Spec {

	void "user guide can be opened via help menu"() {
		when:
		$("#navHelpLink").click()
		then:
		waitFor { $("#navUserGuideLink").displayed }

		when:
		$("#navUserGuideLink").click()
		then:
		waitFor { at UserGuidePage }

		when: "click on last top-level title of user guide"
		def link = tableOfContents.children().last().children("a")
		link.click()
		then: "the corresponding header should be visible"
		$("h1", text:link.text()).displayed
	}

}
