import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.pages.CanvasListPage
import core.pages.CanvasPage

class InputModuleLiveSpec extends LoginTester1Spec {

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(CanvasMixin)
		this.class.metaClass.mixin(ConfirmationMixin)
	}

	def setup() {
		loadCanvas()
		clearAndResume()
	}

	def cleanup() {
		stopCanvasIfRunning()
	}

	private void loadCanvas() {
		to CanvasListPage
		$(".clickable-table a span", text: "InputModuleLiveSpec").click()
		waitFor {
			at CanvasPage
		}
		waitForCanvasContent()
	}

	private void clearAndResume() {
		ensureRealtimeTabDisplayed()
		resetAndStartCanvas(true)
	}

	void "the switcher sends a message"() {
		def getTable = { findModuleByHash(3).find("table.event-table-module-content tbody") }
		def getSwitcher = { findModuleOnCanvas("Switcher").find("div.switcher div.switcher-inner") }

		expect: "table is first empty"
		getTable().find("tr").size() == 0

		when: "switcher is first clicked"
		getSwitcher().click()

		then: "table gets value true"
		waitFor {
			getTable().find("tr td", text: "true").displayed
		}

		when: "switcher is clicked again"
		getSwitcher().click()

		then: "table gets value false"
		waitFor {
			getTable().find("tr").size() == 2
			getTable().find("tr td", text: "false").displayed
		}
	}

	void "the textField sends a message"() {
		def table = { findModuleByHash(4).find("table.event-table-module-content tbody") }
		def textField = { findModuleOnCanvas("TextField").find(".text-field textarea") }
		def sendButton = { findModuleOnCanvas("TextField").find(".text-field .send-btn") }

		expect: "table is first empty"
		table().find("tr").size() == 0

		when: "value is given and button is first clicked"
		textField().firstElement().clear()
		textField() << "testing"
		sendButton().click()

		then: "table gets value 'testing'"
		waitFor {
			table().find("tr td", text: "testing").displayed
		}
	}

	void "the modules remember their state, except if the canvas is cleared"() {
		def switcher = { findModuleOnCanvas("Switcher").find("div.switcher div.switcher-inner") }
		def textField = { findModuleOnCanvas("TextField").find(".text-field textarea") }
		def sendButton = { findModuleOnCanvas("TextField").find(".text-field .send-btn") }

		when: "the states are changed and refreshed"
		switcher().click()
		textField().firstElement().clear()
		textField() << "test"
		sendButton().click()
		loadCanvas()

		def button = { findModuleOnCanvas("Button").find("button.button-module-button") }
		def switcherTable = { findModuleByHash(3).find("table.event-table-module-content tbody") }
		def textFieldTable = { findModuleByHash(4).find("table.event-table-module-content tbody") }

		then: "the modules load their states and the tables have rows"
		waitFor(20) {
			$(".switcher.checked").size() == 1
			textField().getAttribute("value") == "test"
			button().text() == "test"
			switcherTable().find("tr").size() == 1
			textFieldTable().find("tr").size() == 1
		}

		when: "the live canvas is cleared and resumed"
		stopCanvas()
		loadCanvas()
		startCanvas(true)

		then: "the modules don't have the state anymore"
		waitFor {
			$(".switcher.checked").size() == 0
			textField().text() == ""
			button().text() == "button"
			$(".event-table-module table.event-table-module-content tbody tr").size() == 0
		}
	}

	void "the button doesn't send anything if there's no value in buttonName but sends it after the value comes there"() {
		def table = { findModuleByHash(5).find("table.event-table-module-content tbody") }
		def button = { findModuleOnCanvas("Button").find("button.button-module-button") }
		def textField = { findModuleOnCanvas("TextField").find(".text-field textarea") }
		def sendButton = { findModuleOnCanvas("TextField").find(".text-field .send-btn") }

		expect: "table is first empty"
		table().find("tr").size() == 0

		when: "button is clicked"
		button().click()

		then: "table is still empty"
		waitFor {
			table().find("tr").size() == 0
		}

		when: "textField has sent a value"
		textField().firstElement().clear()
		textField() << "test"
		sendButton().click()

		then: "button also sends the message"
		waitFor {
			table().find("tr").size() == 1
			table().find("tr td", text: "0.0").displayed
		}

		then: "then name of the button changes"
		waitFor {
			button().text() == "test"
		}

		when: "the button is clicked now"
		button().click()

		then: "the button sends another value"
		waitFor {
			table().find("tr").size() == 2
		}

		cleanup:
		stopCanvasIfRunning()
	}
}
