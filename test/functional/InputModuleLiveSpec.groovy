import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.pages.CanvasPage
import core.pages.LiveListPage
import pages.*

class InputModuleLiveSpec extends LoginTester1Spec {

	def setupSpec() {
		super.login()
		to LiveListPage

		addAndWaitModule()
	}
	
	void "the buttonModule can be added"() {

	}
}
