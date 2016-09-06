import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.mixins.KafkaMixin
import core.pages.CanvasPage
import core.pages.StreamConfigurePage
import core.pages.StreamCreatePage
import core.pages.StreamShowPage

@Mixin(KafkaMixin)
@Mixin(CanvasMixin)
@Mixin(ConfirmationMixin)
class RunCSVExportModeSpec extends LoginTester1Spec {

	void "running CSV export mode results in file"() {
		setup: "create stream"
		to StreamCreatePage
		def streamName = "RunCSVExportModeSpec" + System.currentTimeMillis()
		name << streamName
		nextButton.click()
		waitFor { at StreamShowPage }

		and: "produce data to stream"
		String topicId = streamId.text()
		produceAllDataToKafka(topicId)

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

		and: "save sand start canvas in realtime mode"
		ensureRealtimeTabDisplayed()
		startCanvas(true)

		and: "produce data to realtime canvas and stop it"
		produceAllDataToKafka(topicId, 1000)
		stopCanvas()

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
		csvContent.readLines().size() == 100 + 1000
	}

	private void produceAllDataToKafka(String id, int iters = 100) {
		def kafka = makeKafkaProducer()
		(1..iters).each { num ->
			kafka.sendJSON(id, "", System.currentTimeMillis(), '{"key": "key-' + num + '", "value": ' + num + '}')
		}
		closeProducer(kafka)
	}
}
