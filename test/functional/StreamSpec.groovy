import geb.spock.GebReportingSpec

import java.nio.file.Paths

import core.mixins.StreamMixin
import core.pages.*
import spock.lang.*
import core.LoginTester1Spec



@Mixin(StreamMixin)
class StreamSpec extends LoginTester1Spec {

	def setup() {
		to StreamListPage
		openStream("CSVImporterFuncSpec")
		waitFor { at StreamShowPage }
		File file = Paths.get(getClass().getResource("files/test-upload-file.csv").toURI()).toFile()
		fileInput = file
		waitFor { $(".history .control-label", text:"Range").displayed }
		waitFor { $(".history div", text:contains("2015-02-23")).displayed }
		waitFor { $(".history div", text:contains("2015-02-25")).displayed }
	}
	
	void "removing data from a stream works"() {		
		when: "only one day's feed files are removed"
		deleteFeedFilesUpTo("2015-02-23")
		then: "The stream has no data from that day anymore"
		waitFor { !$(".history div", text:contains("2015-02-23")).displayed }
		waitFor { $(".history div", text:contains("2015-02-24")).displayed }

		when: "rest of the feed files are deleted"
		deleteAllFeedFiles()
		then: "the stream has no data anymore"
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


