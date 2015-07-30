import java.nio.file.Paths

import core.LoginTester1Spec
import core.mixins.StreamMixin
import core.pages.StreamListPage
import core.pages.StreamShowPage


class StreamSpec extends LoginTester1Spec {

	def setupSpec() {
		// @Mixin is buggy, don't use it
		StreamSpec.metaClass.mixin(StreamMixin)
	}
	
	def setup() {
		to StreamListPage
		openStream("CSVImporterFuncSpec")
		waitFor { at StreamShowPage }
		File file = Paths.get(getClass().getResource("files/test-upload-file.csv").toURI()).toFile()
		fileInput = file
		waitFor { 
			historyDeleteButton.displayed
			historyStartDate.text() == "2015-02-23"
			historyEndDate.text() == "2015-02-25"
		}
	}
	
	void "removing data from a stream works"() {		
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
		waitFor { $("div.alert.alert-info").displayed }
	}
	
}


