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
class RunCSVExportModeSpec extends LoginTester1Spec {

	@Shared StreamService streamService

	def setupSpec() {
		streamService = createStreamService()
	}

	def cleanupSpec() {
		cleanupStreamService(streamService)
	}

	void "running CSV export mode results in file"() {
		setup: "create stream"
		to StreamCreatePage
		def streamName = "RunCSVExportModeSpec" + System.currentTimeMillis()
		name << streamName
		nextButton.click()
		waitFor { at StreamShowPage }

		and: "produce data to stream"
		String topicId = streamId.text()
		produceAllDataToStream(topicId)

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
		searchAndClick("Chart")
		moveModuleBy("Chart", 100, 100)
		connectEndpoints(findOutput("Stream", "value"), findInput("Chart", "in1"))

		when: "run canvas in CSV export mode"
		ensureHistoricalTabDisplayed()
		runHistoricalDropdownButton.click()
		runCsvExportModeButton.click()
		waitForConfirmation()
		acceptConfirmation()

		then: "download link appears"
		waitFor(30) { !$(".csvDownload a").isEmpty() }

		and: "download link works"
		def csvContent = downloadText($(".csvDownload a").attr("href"))
		csvContent.readLines().size() == 100 + 1 // + 1 is header row
	}

	private void produceAllDataToStream(String id, int iters = 100) {
		Stream stream = new Stream()
		stream.id = id
		(1..iters).each { num ->
			streamService.sendMessage(stream, [key: "key-$num", value: num], 30)
		}
	}
}
