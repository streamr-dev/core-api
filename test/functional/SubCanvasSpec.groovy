import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin

public class SubCanvasSpec extends LoginTester1Spec {

	def setupSpec() {
		// For some reason the annotations don't work so need the below.
		SubCanvasSpec.metaClass.mixin(CanvasMixin)
		SubCanvasSpec.metaClass.mixin(ConfirmationMixin)
	}

	def "Subcanvas is loadable on Canvas module and ui modules work"() {
		String unique = "test" + new Date().getTime()

		loadSignalPath("SubCanvasSpec-top")
		moduleShouldAppearOnCanvas("SubCanvasSpec-sub")
		ensureRealtimeTabDisplayed()
		stopCanvasIfRunning()
		resetAndStartCanvas(true)
		noNotificationsVisible()

		expect: "View Canvas button is displayed on Canvas module"
		waitFor { findModuleOnCanvas("SubCanvasSpec-sub").find(".view-canvas-button").displayed }

		when: "The string is sent to the Canvas module"
		findModuleOnCanvas("TextField").find("textarea").firstElement().clear()
		findModuleOnCanvas("TextField").find("textarea") << unique
		findModuleOnCanvas("TextField").find(".btn-primary").click()

		then: "The Table must show the string"
		waitFor { findModuleOnCanvas("Table").text().contains(unique) }

		when: "View Canvas button is clicked"
		findModuleOnCanvas("SubCanvasSpec-sub").find(".view-canvas-button").click()

		then: "The subcanvas should be shown"
		moduleShouldAppearOnCanvas("Label")
		moduleShouldAppearOnCanvas("Table")

		then: "The ui modules should show the correct content"
		waitFor { findModuleOnCanvas("Label").find(".modulelabel").text() == unique }
		waitFor { findModuleOnCanvas("Table").find("table.event-table-module-content tbody tr").size() == 1 }
		waitFor { findModuleOnCanvas("Table").find("table.event-table-module-content tbody tr").text().contains(unique) }

		/**
		 * Check that the subcanvas is not using the original saved Canvas' uiChannels
		 */
		when: "Load and start subcanvas to subscribe the uiChannels"
		loadSignalPath("SubCanvasSpec-sub")
		ensureRealtimeTabDisplayed()
		stopCanvasIfRunning()
		resetAndStartCanvas(true)
		noNotificationsVisible()

		then: "The unique string for this test must not be shown on the label and table"
		findModuleOnCanvas("Label").find(".modulelabel")?.text() != unique
		!findModuleOnCanvas("Table").find("table.event-table-module-content tbody tr")?.text()?.contains(unique)

		cleanup:
		loadSignalPath("SubCanvasSpec-top")
		stopCanvasIfRunning()
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

		then: "Subcanvas select should appear"
		waitFor {
			findModuleOnCanvas("ForEach").find(".subcanvas-select")
		}
		findModuleOnCanvas("ForEach").find(".subcanvas-select option").size() == 1
		findModuleOnCanvas("ForEach").find(".subcanvas-select option").find { it.value() == unique }.click()

		when: "View Canvas button is clicked"
		findModuleOnCanvas("ForEach").find(".view-canvas-button").click()

		then: "The subcanvas should be shown"
		moduleShouldAppearOnCanvas("Label")
		moduleShouldAppearOnCanvas("Table")

		then: "The ui modules should show the correct content"
		waitFor { findModuleOnCanvas("Label").find(".modulelabel").text() == unique }
		waitFor { findModuleOnCanvas("Table").find("table.event-table-module-content tbody tr").size() == 1 }
		waitFor { findModuleOnCanvas("Table").find("table.event-table-module-content tbody tr").text().contains(unique) }

		cleanup:
		loadSignalPath("SubCanvasSpec-top")
		stopCanvasIfRunning()
	}

}
