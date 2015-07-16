import java.nio.file.Paths

import spock.lang.*
import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.StreamMixin
import core.pages.*



@Mixin(StreamMixin)
@Mixin(CanvasMixin)
class CSVImporterFuncSpec extends LoginTester1Spec {

	void "uploading data from csv with a non-supported date format to a stream works"() {
		when: "Go to StreamListPage"
		to StreamListPage
		then: "The previously created testing stream can be found"
		waitFor { $("table td", text:contains("CSVImporterFuncSpec")).displayed }
		
		when: "Stream is opened"
		openStream("CSVImporterFuncSpec")
		then: "Go to StreamShowPage"
		waitFor { at StreamShowPage }
		
		when: "The file to be uploaded is configured"
		File file = Paths.get(getClass().getResource("files/epoch-test.csv").toURI()).toFile()
		then:
		file.exists()
		
		when: "File is uploaded to the dropzone"
		fileInput = file
		then: "Go to StreamConfirmPage"
		waitFor{ at StreamConfirmPage }
		
		when: "Time format Java Timestamp is selected and clicked to confirm"
		$(".lbl", text:contains("Java timestamp")).click()
		$("#submit").click()
		then: "The data is uploaded"
		waitFor { at StreamShowPage }
		$(".col-sm-12 table td", text:"Begin Date").displayed
		$(".col-sm-12 table td", text:"2015-04-30").displayed
		$(".col-sm-12 table td", text:"2015-05-01").displayed
		$(".col-sm-12 table td", text:"2015-05-02").displayed
		$(".col-sm-12 table td", text:"2015-05-03").displayed
		$("#stream-fields tbody td")[0].text() == "Product Title"
		$("#stream-fields tbody td")[2].text() == "Price"
		$("#stream-fields tbody td")[4].text() == "Rootstock"
		$("#stream-fields tbody td")[6].text() == "Height"
		$("#stream-fields tbody td")[8].text() == "Description"
		$("#stream-fields tbody td")[1].text() == "string"
		$("#stream-fields tbody td")[3].text() == "number"
		$("#stream-fields tbody td")[5].text() == "string"
		$("#stream-fields tbody td")[7].text() == "number"
		$("#stream-fields tbody td")[9].text() == "string"
		
		//The stream must be left empty
		emptyStream("CSVImporterFuncSpec")
	}
	
	void "uploading data from a csv with a supported date format works"() {
		to StreamListPage
		
		when: "The previously created testing stream is clicked to open"
		$("table td", text:contains("CSVImporterFuncSpec")).click()
		then: "Go to StreamShowPage"
		waitFor { at StreamShowPage }
		
		when: "A another testing file is configured to be uploaded"
		File file = Paths.get(getClass().getResource("files/test-upload-file.csv").toURI()).toFile()
		then:
		file.exists()
		
		when: "File is uploaded"
		fileInput = file
		then: "the correct data is uploaded"
		waitFor(){ at StreamShowPage }
		waitFor(10){ $(".col-sm-12 table td", text:"Begin Date").displayed }
		$(".col-sm-12 table td", text:"Begin Date").displayed
		$(".col-sm-12 table td", text:"2015-02-23").displayed
		$(".col-sm-12 table td", text:"2015-02-24").displayed
		$(".col-sm-12 table td", text:"2015-02-25").displayed
		$("#stream-fields tbody td")[0].text() == "price"
		$("#stream-fields tbody td")[2].text() == "size"
		$("#stream-fields tbody td")[4].text() == "really"
		$("#stream-fields tbody td")[6].text() == "comment"
		$("#stream-fields tbody td")[1].text() == "number"
		$("#stream-fields tbody td")[3].text() == "number"
		$("#stream-fields tbody td")[5].text() == "boolean"
		$("#stream-fields tbody td")[7].text() == "string"
	}
	
	void "the data of the stream can now be used e.g. in canvas"() {
		when: "Selected the correct dates that contain data and configured modules to test it"
		$("#beginDate").firstElement().clear()
		$("#beginDate") << "2015-02-23"
		$("#endDate").firstElement().clear()
		$("#endDate") << "2015-02-25"
		$(".input-group-addon", text:"To").click()
		
		searchAndClickContains("CSVImporterFuncSpec")
		searchAndClick("Label")
		
		connectEndpoints(findOutput("Stream", "price"), findInput("Label", "label"))
		runButton.click()
		
		then: "The label module has some content -> stream has data"
		waitFor(15){ $(".module .modulebody .modulelabel").text() != "" }
		
		//The stream must be left empty
		emptyStream("CSVImporterFuncSpec")
	}
	
}


