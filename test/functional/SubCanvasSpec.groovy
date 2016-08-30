import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin

public class SubCanvasSpec extends LoginTester1Spec {

	def setupSpec() {
		// For some reason the annotations don't work so need the below.
		SubCanvasSpec.metaClass.mixin(CanvasMixin)
		SubCanvasSpec.metaClass.mixin(ConfirmationMixin)
	}

	def "Subcanvas is loadable on ForEach module and ui modules work"() {
		String unique = "test" + new Date().getTime()

		loadSignalPath("SubCanvasSpec-top")
		moduleShouldAppearOnCanvas("ForEach")
		ensureRealtimeTabDisplayed()
		stopCanvasIfRunning()
		resetAndStartCanvas(true)
		noNotificationsVisible()

		when: "The string is sent to the ForEach module"
		findModuleOnCanvas("TextField").find("textarea").firstElement().clear()
		findModuleOnCanvas("TextField").find("textarea") << unique
		findModuleOnCanvas("TextField").find(".btn-primary").click()

		then: "The Table must show the string"
		waitFor { findModuleOnCanvas("Table").text().contains(unique) }

		when: "The refresh button on the ForEach module is clicked"
		findModuleOnCanvas("ForEach").find(".modulebutton .refresh").click()

		then: "Subcanvas selector should appear"
		waitFor {
			findModuleOnCanvas("ForEach").find(".subcanvas-selector")
		}
		findModuleOnCanvas("ForEach").find(".subcanvas-selector option").size() == 1
		findModuleOnCanvas("ForEach").find(".subcanvas-selector option").find { it.value() == unique }.click()

		when: "Show button is clicked"
		findModuleOnCanvas("ForEach").find(".subcanvas-selector button").click()

		then: "The subcanvas should be shown"
		moduleShouldAppearOnCanvas("Label")
		moduleShouldAppearOnCanvas("Table")

		then: "The ui modules should show the correct content"
		waitFor { findModuleOnCanvas("Label").find(".modulelabel").text() == unique }
		waitFor { findModuleOnCanvas("Table").find("table.event-table-module-content tbody tr").size() == 1 }
		findModuleOnCanvas("Table").find("table.event-table-module-content tbody tr").text().contains(unique)
	}

}
