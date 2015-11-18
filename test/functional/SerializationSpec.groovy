import spock.lang.*
import com.unifina.controller.core.signalpath.LiveController
import com.unifina.datasource.RealtimeDataSource
import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.service.BootService
import com.unifina.utils.MapTraversal
import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.pages.LiveShowPage
import grails.test.mixin.TestFor

@Mixin(CanvasMixin)
@Mixin(ConfirmationMixin)
@TestFor(LiveController)
class SerializationSpec extends LoginTester1Spec {

	static UnifinaKafkaProducer kafka

	def setupSpec() {
		BootService.mergeDefaultConfig(grailsApplication)
		kafka = new UnifinaKafkaProducer(makeKafkaConfiguration())

		// For some reason the annotations don't work so need the below.
		SerializationSpec.metaClass.mixin(CanvasMixin)
		SerializationSpec.metaClass.mixin(ConfirmationMixin)
	}

	def cleanupSpec() {
		synchronized(kafka) {
			kafka.close()
		}
	}

	def "resuming paused live canvas retains modules' states"() {
		String liveName = "test" + new Date().getTime()

		when: "Modules are added and clicked 'Launch live'"
			// The stream
			searchAndClick("SerializationSpec")
			moduleShouldAppearOnCanvas("Stream")
			searchAndClick("Count")
			moduleShouldAppearOnCanvas("Count")
			searchAndClick("Sum")
			moduleShouldAppearOnCanvas("Sum")
			searchAndClick("Add")
			moduleShouldAppearOnCanvas("Add")
			searchAndClick("Label")
			moduleShouldAppearOnCanvas("Label")

			connectEndpoints(findOutput("Stream", "a"), findInput("Count", "in"))
			connectEndpoints(findOutput("Stream", "b"), findInput("Sum", "in"))
			connectEndpoints(findOutput("Count", "count"), findInput("Add", "in1"))
			connectEndpoints(findOutput("Sum", "out"), findInput("Add", "in2"))
			connectEndpoints(findOutput("Add", "sum"), findInput("Label", "label"))

			$("#runDropdown").click()
			waitFor { $("#runLiveModalButton").displayed }
			$("#runLiveModalButton").click()
		then: "launch live -modal opens"
			waitFor { $("#runLiveModal").displayed }

		when: "Name for live canvas is given and it is launched"
			$("#runLiveName") << liveName
			$("#runLiveButton").click()
		then: "LiveShowPage is opened and Label shows data"
			waitFor(30) { at LiveShowPage }
			stopButton.displayed
			!$(".alert").displayed

			Thread.start {
				for (int i = 0; i < 20; ++i) {
					kafka.sendJSON("mvGKMdDrTeaij6mmZsQliA",
						"", System.currentTimeMillis(),
						'{"a":' + i + ', "b": ' + (i * 0.5)  + '}')
					sleep(150)
				}
			}

			// Wait for enough data, sometimes takes more than 30 sec to come
			waitFor(30) { $(".modulelabel").text().toDouble() == 115.0D }
			sleep(RealtimeDataSource.SERIALIZATION_INTERVAL_IN_SECS * 1200)
			def oldVal = $(".modulelabel").text().toDouble()

		when: "Live canvas is stopped"
			stopButton.click()
		then: "The confirmation dialog is shown"
			waitForConfirmation()

		when: "Clicked OK"
			acceptConfirmation()
		then: "The LiveShowPage is opened again, now with the start and delete -buttons and info alert"
			waitFor(30) {
				startButton.displayed
				deleteButton.displayed
				$(".alert.alert-info").displayed
			}

		when: "Started again"
			startButton.click()
		then: "The LiveShowPage is opened and data must change"
			waitFor { at LiveShowPage }

			Thread.start {
				for (int i = 100; i < 105; ++i) {
					kafka.sendJSON("mvGKMdDrTeaij6mmZsQliA",
						"", System.currentTimeMillis(),
						'{"a":' + i + ', "b": ' + (i * 0.5)  + '}')
					sleep(150)
				}
			}

			waitFor(30){ $(".modulelabel").text().toDouble() == (oldVal + 5 + 255).toDouble()}
	}

	private def makeKafkaConfiguration() {
		Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(grailsApplication.config, "unifina.kafka"));
		Properties properties = new Properties();
		for (String s : kafkaConfig.keySet()) {
			properties.setProperty(s, kafkaConfig.get(s).toString());
		}
		return properties;
	}
}
