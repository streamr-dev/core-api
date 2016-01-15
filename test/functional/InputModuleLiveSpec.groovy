import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.pages.CanvasPage
import core.pages.LiveListPage
import core.pages.LiveShowPage
import pages.*

class InputModuleLiveSpec extends LoginTester1Spec {

	void openLiveCanvas() {
		to LiveListPage
		$(".clickable-table a span", text: "InputModuleLiveSpec").click()
		waitFor {
			at LiveShowPage
		}
	}

	void clearAndResume() {
		if(!$("#startButton").displayed) {
			$("#stopButton").click()
			waitFor {
				$("div.modal-content").displayed
			}
			$(".modal-content div.modal-footer button.btn-primary", text: "OK").click()
			waitFor {
				$("#startButton").displayed
			}
		}
		$("#runDropdown").click()
		waitFor {
			$("#clearAndStartButton").displayed
		}
		$("#clearAndStartButton").click()
		waitFor {
			$("div.modal-content").displayed
		}
		$(".modal-content div.modal-footer button.btn-primary", text: "OK").click()
		waitFor {
			$("#stopButton").displayed
		}
	}

	def setup() {
		openLiveCanvas()
		clearAndResume()
	}
	
	void "the switcher sends a message"() {
		def table = $("#module_3 table.event-table-module-content tbody")
		def switcher = $("div.switcher div.switcher-inner")
		expect: "table is first empty"
		table.find("tr").size() == 0

		when: "switcher is first clicked"
		switcher.click()
		then: "table gets value 1.0"
		waitFor {
			table.find("tr", 0).find("td", 1).text() == "1.0"
		}

		when: "switcher is clicked again"
		switcher.click()
		then: "table gets value 0.0"
		waitFor {
			table.find("tr").size() == 2
			table.find("tr", 0).find("td", 1).text() == "0.0"
		}
	}

	void "the textField sends a message"() {
		def table = $("#module_4 table.event-table-module-content tbody")
		def textField = $(".text-field textarea")
		def sendButton = $(".text-field .send-btn")
		expect: "table is first empty"
		table.find("tr").size() == 0

		when: "value is given and button is first clicked"
		textField << "testing"
		sendButton.click()
		then: "table gets value 'testing'"
		waitFor {
			table.find("tr", 0).find("td", 1).text() == "testing"
		}
	}

	void "the modules remember their state, except if the canvas is cleared"() {
		def switcher = $("div.switcher div.switcher-inner")
		def textField = $(".text-field textarea")
		def sendButton = $(".text-field .send-btn")
		when: "the states are changed and refreshed"
		switcher.click()
		textField << "test"
		sendButton.click()
		driver.navigate().refresh()

		// Because of the refresh the references must be set again
		def button = $("button.button-module-button")
		then: "the modules still have their states but the tables are empty"
		$(".switcher.checked").size() == 1
		// Geb's own .text() didn't work for some reason
		js.exec("return \$('.text-field textarea').val()") == "test"
		button.text() == "test"
		$(".event-table-module table.event-table-module-content tbody tr").size() == 2

		when: "the live canvas is cleared and resumed"
		clearAndResume()

		// Because of the refresh the references must be set again
		button = $("button.button-module-button")
		textField = $(".text-field textarea")
		then: "the modules don't have the state anymore"
		$(".switcher.checked").size() == 0
		textField.text() == ""
		button.text() == "button"
		$(".event-table-module table.event-table-module-content tbody tr").size() == 0
	}

	void "the button doesn't send anything if there's no value in buttonName but sends it after the value comes there"() {
		def table = $("#module_5 table.event-table-module-content tbody")
		def button = $("button.button-module-button")
		def textField = $(".text-field textarea")
		def sendButton = $(".text-field .send-btn")
		expect: "table is first empty"
		table.find("tr").size() == 0
		when: "button is clicked"
		button.click()
		then: "table is still empty"
		waitFor {
			table.find("tr").size() == 0
		}
		when: "textField has sent a value"
		textField << "test"
		sendButton.click()
		then: "button also sends the message"
		waitFor {
			table.find("tr").size() == 1
			table.find("tr", 0).find("td", 1).text() == "0.0"
		}
		then: "then name of the button changes"
		button.text() == "test"
		when: "the button is clicked now"
		button.click()
		then: "the button sends another value"
		waitFor {
			table.find("tr").size() == 2
		}
	}
}
