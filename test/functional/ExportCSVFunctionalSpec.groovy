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
class ExportCSVFunctionalSpec extends LoginTester1Spec {

	void "ExportCSV module produces a file"() {
		setup: "create stream"
		to StreamCreatePage
		def streamName = "ExportCSVFunctionalSpec" + System.currentTimeMillis()
		name << streamName
		nextButton.click()
		waitFor { at StreamShowPage }

		and: "produce data to stream"
		String topicId = streamId.text()
		produceAllDataToKafka(topicId, 5)

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

		and: "save sand start canvas in realtime mode"
		ensureRealtimeTabDisplayed()
		startCanvas(true)

		and: "produce data to realtime canvas and stop it"
		produceAllDataToKafka(topicId, 1000)
		stopCanvas()

		when: "download link appears"
		waitFor(30) { !$(".moduleDownloadLink a").isEmpty() }

		then: "download link works"
		def csvContent = downloadText($(".moduleDownloadLink a").attr("href"))
		csvContent.readLines().size() == 1000 + 1 // data + header
	}

	private void produceAllDataToKafka(String id, int iters) {
		def kafka = makeKafkaProducer()
		(1..iters).each { num ->
			kafka.sendJSON(id, "", System.currentTimeMillis(), '{"key": "key-' + num + '", "value": ' + num + '}')
		}
		closeProducer(kafka)
	}
}
