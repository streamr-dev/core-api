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

	def setup() {
		openLiveCanvas()
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
		when: "Clear and resume"
		$("#runDropdown").click()
		waitFor {
			$("#clearAndStartButton").displayed
		}
		$("#clearAndStartButton").click()
		waitFor {
			$("div.modal-content").displayed
		}
		$(".modal-content div.modal-footer button.btn-primary", text: "OK").click()
		then: "Canvas is running"
		waitFor {
			$("#stopButton").displayed
		}
	}
	
	void "the switcher sends a message"() {
		def table = $("#module_3 table.event-table-module-content tbody")
		def switcher = $("div.switcher div.switcher-inner")
		expect: "table is first empty"
		table.find("tr").size() == 0

		when: "switcher is first clicked"
		switcher.click()
		then: "table gets value 1.0"
		table.find("tr", 0).find("td", 1).text() == "1.0"

		when: "switcher is clicked again"
		switcher.click()
		then: "table gets value 0.0"
		table.find("tr").size() == 2
		table.find("tr", 0).find("td", 1).text() == "0.0"
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
		table.find("tr").size() == 0
		when: "textField has sent a value"
		textfield = "test"
		sendButton.click()
		then: "button also sends the message"
		table.find("tr").size() == 1
		table.find("tr", 0).find("td", 1).text() == "10"
		then: "then name of the button changes"
		button.text() == "test"
		when: "the button is clicked now"
		button.click()
		then: "the button sends another value"
		table.find("tr").size() == 2
	}
}
