import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin

public class SubCanvasSpec extends LoginTester1Spec {

	private final static String topCanvasName = "SubCanvasSpec-top"
	private final static String subCanvasName = "SubCanvasSpec-sub"

	def setupSpec() {
		// For some reason the annotations don't work so need the below.
		SubCanvasSpec.metaClass.mixin(CanvasMixin)
		SubCanvasSpec.metaClass.mixin(ConfirmationMixin)
	}

	def setup() {
		loadSignalPath(topCanvasName)
		ensureRealtimeTabDisplayed()
		stopCanvasIfRunning()
		resetAndStartCanvas(true)
		noNotificationsVisible()
	}

	def cleanup() {
		loadSignalPath(topCanvasName)
		stopCanvasIfRunning()
	}

	def "Subcanvas is loadable on Canvas module and ui modules work"() {
		String unique = "test" + new Date().getTime()

		expect: "View Canvas button is displayed on Canvas module"
		waitFor { findModuleOnCanvas(subCanvasName).find(".view-canvas-button").displayed }

		when: "The string is sent to the Canvas module"
		findModuleOnCanvas("TextField").find("textarea").firstElement().clear()
		findModuleOnCanvas("TextField").find("textarea") << unique
		findModuleOnCanvas("TextField").find(".btn-primary").click()

		then: "The Table must show the string"
		waitFor { findModuleOnCanvas("Table").text().contains(unique) }

		when: "View Canvas button is clicked"
		Thread.sleep(1000) // allow time for message to be sent to stream
		findModuleOnCanvas(subCanvasName).find(".view-canvas-button").click()

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
		when: "Load and  subcanvas to subscribe the uiChannels"
		loadSignalPath(subCanvasName)
		ensureRealtimeTabDisplayed()
		stopCanvasIfRunning()
		resetAndStartCanvas(true)
		noNotificationsVisible()

		then: "The unique string for this test must not be shown on the label and table"
		findModuleOnCanvas("Label").find(".modulelabel")?.text() != unique
		!findModuleOnCanvas("Table").find("table.event-table-module-content tbody tr")?.text()?.contains(unique)
	}

	def "Subcanvas is loadable on ForEach module and ui modules work"() {
		String unique = "test" + new Date().getTime()

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
	}

}
