import core.LoginTester1Spec;
import geb.spock.GebReportingSpec
import spock.lang.*
import pages.*
import core.mixins.CanvasMixin


@Mixin(CanvasMixin)
public class LiveSpec extends LoginTester1Spec {

	def "launching, modifying and deleting live canvas works correctly"() {
		// Unique name for the live
		String liveName = "test" + new Date().getTime()
		
		when: "Modules are added and clicked 'Launch live'"
		// The stream
		searchAndClickContains("Live test")
		searchAndClickContains("label")
		def label = $(".modulename", text:"Label")
		connectEndpoints(findOutput("Stream", "val"), findInput("Label", "label"))
		$("#runDropdown").click()
		waitFor { $("#runLiveModalButton").displayed }
		$("#runLiveModalButton").click()
		then: "launch live -modal opens"
		waitFor { $("#runLiveModal").displayed }
		
		when: "Name for live canvas is given and it is launched"
		$("#runLiveName") << liveName
		$("#runLiveButton").click()
		then: "LiveShowPage is opened"
		waitFor { at LiveShowPage }
		stopButton.displayed
	
		when: "Live canvas is stopped"
		stopButton.click()
		then: "The confirmation dialog is shown"
		waitFor { $(".modal-dialog").displayed }
		when: "Clicked OK"
		confirmOkButton.click()
		then: "The LiveShowPage is opened again, now with the start and delete -buttons"
		waitFor{ at LiveShowPage }
		startButton.displayed
		deleteButton.displayed
		
		when: "Started again"
		startButton.click()
		then: "The LiveShowPage is opened and some data is shown"
		waitFor { at LiveShowPage }
		// Wait for data, sometimes takes more than 30sec to come
		// Only label has '.modulelabel'
		waitFor(30){ $(".modulelabel").text() != "" }
	
		when: "Went to the LiveListPage"
		to BuildPage
		to LiveListPage
		then: "The just created live canvas can be found"
		waitFor { at LiveListPage }
		$("table td", text:liveName).displayed
		
		when: "Clicked to open the just created live canvas"
		$("table td", text:liveName).click()
		then: "The LiveShowPage is opened"
		waitFor { at LiveShowPage }
		stopButton.displayed
		
		when: "Clicked to stop"
		stopButton.click()
		then: "The confirmation dialog is shown"
		waitFor { $(".modal-dialog").displayed }
		when: "Clicked OK"
		confirmOkButton.click()
		then: "The liveShowPage is opened again with the start and delete -buttons"
		waitFor{ at LiveShowPage }
		startButton.displayed
		deleteButton.displayed
		
		when: "Clicked to delete"
		deleteButton.click()
		then: "Confirmation dialog is opened"
		waitFor { $(".modal-dialog").displayed }
		when: "Clicked OK"
		confirmOkButton.click()
		then: "LiveListPage is opened, and the just created (and deleted) live canvas cannot be found from there anymore"
		waitFor{ at LiveListPage }
		waitFor { !($("table td", text:liveName).displayed) }
	}


}
