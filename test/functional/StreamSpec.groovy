import geb.spock.GebReportingSpec

import java.nio.file.Paths

import core.mixins.StreamMixin
import core.pages.*
import spock.lang.*



@Mixin(StreamMixin)
class StreamSpec extends LoginTester1Spec {

	def setup() {
		to StreamListPage
		openStream("CSVImporterFuncSpec")
		waitFor { at StreamShowPage }
		File file = Paths.get(getClass().getResource("files/test-upload-file.csv").toURI()).toFile()
		fileInput = file
		waitFor { $("td", text:"Begin Date").displayed }
	}
	
	void "removing data from a stream works"() {		
		when: "Feed files are deleted by clicking 'SelectAll' button"
		deleteFeedFiles()
		then: "The stream has no history anymore"
		$(".col-sm-12 .col-sm-6 p", text:contains("no history"))
		
		when: "Clicked to go to the configure view and deleted the fields"
		$(".btn.btn-sm", text:"Configure Fields").click()
		waitFor { $("span.btn.btn-sm.delete").size() == 4 }
		deleteFields()
		then: "There are no fields anymore"
		waitFor { $("span.btn.btn-sm.delete").size() == 0 }
		
		when: "Saved"
		$(".btn.save", text:"Save").click()
		then: "Go to StreamShowPage, no configured fields"
		waitFor { at StreamShowPage }
		waitFor { $("div.alert", text:"The fields for this stream are not yet configured. Click the button above to configure them.").displayed }
	}
	
	
	
}


