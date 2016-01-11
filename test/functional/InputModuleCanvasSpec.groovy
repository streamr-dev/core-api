import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.pages.*
import pages.*

class InputModuleCanvasSpec extends LoginTester1Spec {

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(CanvasMixin)
		
		super.login()
		waitFor { at CanvasPage }

		addAndWaitModule()
	}
	
	void "the buttonModule can be added"() {

	}
}
