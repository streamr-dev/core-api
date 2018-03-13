import LoginTester1Spec
import mixins.CanvasMixin
import mixins.StreamMixin
import pages.StreamConfirmPage
import pages.StreamListPage
import pages.StreamShowPage

import java.nio.file.Paths

class CSVImporterFuncSpec extends LoginTester1Spec implements CanvasMixin, StreamMixin {

	private File getFile(String filename) {
		return Paths.get(getClass().getResource("files/$filename").toURI()).toFile()
	}

	def cleanupSpec() {
		super.login()
		// The stream must be left empty
		emptyStream("CSVImporterFuncSpec")
	}

	void "uploading data from csv with a non-supported date format to a stream works"() {
		setup:
		// Just made sure that the stream is empty
		emptyStream("CSVImporterFuncSpec")

		when: "Go to StreamListPage"
		to StreamListPage
		then: "The previously created testing stream can be found"
		streamExists("CSVImporterFuncSpec")
		
		when: "Stream is opened"
		openStream("CSVImporterFuncSpec")
		then: "Go to StreamShowPage"
		waitFor { at StreamShowPage }
		
		when: "The file to be uploaded is configured"
		fileInput = getFile("epoch-test.csv")
		then: "Go to StreamConfirmPage"
		waitFor{ at StreamConfirmPage }
		
		when: "Time format Java Timestamp is selected and clicked to confirm"
		$(".lbl", text:contains("Java timestamp")).click()
		$("#submit").click()
		then: "The data is uploaded"
		waitFor { at StreamShowPage }
		waitFor { $(".history .control-label", text:"Range").displayed }
		waitFor { $(".history div", text:contains("2015-04-30"))[0].displayed }
		waitFor { $(".history div", text:contains("2015-05-03"))[0].displayed }

		emptyStream("CSVImporterFuncSpec")
	}
	
	void "uploading data from a csv with a supported date format works"() {
		to StreamListPage
		
		when: "The previously created testing stream is clicked to open"
		openStream("CSVImporterFuncSpec")
		then: "Go to StreamShowPage"
		waitFor { at StreamShowPage }
		
		when: "A another testing file is configured to be uploaded"
		fileInput = getFile("test-upload-file.csv")
		then: "the correct data is uploaded"
		waitFor(30) { $(".history .control-label", text:"Range").displayed }
		waitFor { $(".history div", text:contains("2015-02-23"))[0].displayed }
		waitFor { $(".history div", text:contains("2015-02-25"))[0].displayed }
	}
	
	void "the data of the stream can now be used e.g. in canvas"() {
		when: "Selected the correct dates that contain data and configured modules to test it"
		$("#beginDate").firstElement().clear()
		$("#beginDate") << "2015-02-23"
		$("#endDate").firstElement().clear()
		$("#endDate") << "2015-02-25"
		$(".input-group-addon", text:"To").click()
		
		searchAndClickContains("CSVImporter")
		moduleShouldAppearOnCanvas("Stream")
		searchAndClick("Label")
		moduleShouldAppearOnCanvas("Label")
		
		connectEndpoints(findOutput("Stream", "price"), findInput("Label", "label"))
		runHistoricalButton.click()
		
		then: "The label module has some content -> stream has data"
		waitFor(15){ $(".module .modulebody .modulelabel").text() != "" }
	}
	
}


