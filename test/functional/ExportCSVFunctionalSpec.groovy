import com.unifina.controller.core.signalpath.CanvasController
import com.unifina.domain.data.Stream
import com.unifina.service.StreamService
import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.mixins.StreamMixin
import core.pages.CanvasPage
import core.pages.StreamConfigurePage
import core.pages.StreamCreatePage
import core.pages.StreamShowPage
import grails.test.mixin.TestFor
import spock.lang.Shared

@Mixin(CanvasMixin)
@Mixin(ConfirmationMixin)
@Mixin(StreamMixin)
@TestFor(CanvasController) // for JSON conversion to work

class ExportCSVFunctionalSpec extends LoginTester1Spec {

	@Shared StreamService streamService

	def setupSpec() {
		streamService = createStreamService()
	}

	def cleanupSpec() {
		cleanupStreamService(streamService)
	}

	void "ExportCSV module produces a file"() {
		setup: "create stream"
		createStream("ExportCSVFunctionalSpec" + System.currentTimeMillis())

		and: "produce data to stream"
		String topicId = streamId.text()
		produceAllDataToStream(topicId, 5)
		sleep(1000)

		and: "configure stream with autodetect"
		configureFieldsButton.click()
		waitFor { at StreamConfigurePage }
		autodetectButton.click()
		waitFor {
			fieldsTable.find("tbody tr").size() == 2
		}
		saveButton.click()
		waitFor { at StreamShowPage }

		and: "create canvas"
		to CanvasPage
		searchAndClick(streamName)
		searchAndClick("ExportCSV")
		moveModuleBy("ExportCSV", 100, 100)
		connectEndpoints(findOutput("Stream", "key"), findInputByDisplayName("ExportCSV", "in1"))
		connectEndpoints(findOutput("Stream", "value"), findInputByDisplayName("ExportCSV", "in2"))

		and: "save and start canvas in realtime mode"
		ensureRealtimeTabDisplayed()
		startCanvas(true)

		and: "produce data to realtime canvas and stop it"
		produceAllDataToStream(topicId, 1000)
		stopCanvas()

		when: "download link appears"
		waitFor(30) { !$(".moduleDownloadLink a").isEmpty() }

		then: "download link works"
		def csvContent = downloadText($(".moduleDownloadLink a").attr("href"))
		csvContent.readLines().size() == 1000 + 1 // data + header
	}

	private void produceAllDataToStream(String id, int iters) {
		Stream stream = new Stream()
		stream.id = id
		(1..iters).each { num ->
			streamService.sendMessage(stream, [key: "key-$num", value: num], 30)
		}
	}
}
