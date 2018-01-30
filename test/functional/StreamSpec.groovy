import com.unifina.domain.data.Stream
import com.unifina.service.StreamService
import LoginTester1Spec
import mixins.ConfirmationMixin
import mixins.StreamMixin
import pages.*
import spock.lang.Shared

import java.nio.file.Paths

class StreamSpec extends LoginTester1Spec implements ConfirmationMixin, StreamMixin {

	@Shared StreamService streamService

	def setupSpec() {
		streamService = createStreamService()
	}

	def cleanupSpec() {
		cleanupStreamService(streamService)
	}
	
	private File getFile(String filename) {
		// The test csv files must be available in the local filesystem of the machine where the browser is running.
		// Note that it's impossible to check here whether the file exists because this code runs on a different machine.
		boolean inJenkins = (System.getenv('BUILD_NUMBER') != null)
		return inJenkins ? new File("/vagrant/$filename") : Paths.get(getClass().getResource("files/$filename").toURI()).toFile() 
	}
	
	void "removing data from a stream works"() {
		setup:
			to StreamListPage
			openStream("CSVImporterFuncSpec")
			waitFor { at StreamShowPage }
			fileInput = getFile("test-upload-file.csv")
			waitFor {
				at StreamShowPage
				historyDeleteButton.displayed
				historyStartDate.text() == "2015-02-23"
				historyEndDate.text() == "2015-02-25"
			}
		
		when: "only one day's feed files are removed"
			deleteFeedFilesUpTo("2015-02-23")
		then: "The stream has no data from that day anymore"
			waitFor { historyStartDate.text() == "2015-02-24" }

		when: "rest of the feed files are deleted"
			deleteAllFeedFiles()
		then: "the stream has no data anymore"
			waitFor { noHistoryMessage.displayed }

		
		when: "Clicked to go to the configure view and deleted the fields"
			configureFieldsButton.click()
		waitFor { $(".delete-field-button").size() == 4 }
			deleteFields()
		then: "There are no fields anymore"
			waitFor { $(".delete-field-button").size() == 0 }
		
		when: "Saved"
			$("button.save").click()
		then: "Go to StreamShowPage, no configured fields"
			waitFor { at StreamShowPage }
			waitFor { $("div.alert.alert-info")[0].displayed }
	}
	
	void "creating streams and autodetecting fields"() {
		setup:
			def streamName = "StreamSpec"+System.currentTimeMillis()
			to StreamListPage
			waitFor { at StreamListPage }
		
		when: "create stream button is clicked"
			createButton.click()
		then: "must go to stream create page"
			waitFor { at StreamCreatePage }
			
		when: "name and desc are entered and next button is clicked"
			name << streamName
			description << streamName + " description"
			nextButton.click()
		then: "must navigate to stream show page, showing info about non-configured stream"
			waitFor { at StreamShowPage }
			$(".alert-info", text: contains('configure'))

		when: "Configure Fields button is clicked"
			def streamId = $(".stream-id").text()
			configureFieldsButton.click()
		then: "Navigate to configure page"
			waitFor { at StreamConfigurePage }

		when: "Produce an event into the stream and click autodetect button"
			Stream testStream = new Stream()
			testStream.id = streamId
			streamService.sendMessage(testStream, [foo: "bar", "xyz": 45.5], 30)
			sleep(1000)
			autodetectButton.click()
		then: "The fields in the stream must appear and be of correct type"
			waitFor {
					$("input", name:"field_name").size() == 2
					$("select", name:"field_type").size() == 2
					$("select", name:"field_type").getAt(0).value() == "string"
					$("select", name:"field_type").getAt(1).value() == "number"
					$(".delete-field-button").size() == 2
			}
			
		when: "save button is clicked"
			saveButton.click()
		then: "navigate back to show page, showing the fields and message"
			waitFor { at StreamShowPage }
			$(".alert-info").displayed
			$("#stream-fields tbody tr").size() == 2

		when: "open menu"
			streamMenuButton.click()
		then: "delete in menu"
			waitFor { deleteStreamButton.displayed }

		when: "delete stream button is clicked"
			deleteStreamButton.click()
		then: "must show confirmation"
			waitForConfirmation()
			
		when: "confirmation accepted"
			acceptConfirmation()
		then: "must navigate to list page and show message"
			waitFor { at StreamListPage }
	}
}


