import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.utils.MapTraversal
import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import grails.util.Holders

@Mixin(CanvasMixin)
@Mixin(ConfirmationMixin)
class VariadicEndpointsSpec extends LoginTester1Spec {
	UnifinaKafkaProducer kafka

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(CanvasMixin)
		this.class.metaClass.mixin(ConfirmationMixin)
	}

	def setup() {
		kafka = new UnifinaKafkaProducer(makeKafkaConfiguration())
	}

	def cleanup() {
		synchronized(kafka) {
			kafka.close()
		}
	}

	private def makeKafkaConfiguration() {
		Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(Holders.config, "unifina.kafka"));
		Properties properties = new Properties();
		for (String s : kafkaConfig.keySet()) {
			properties.setProperty(s, kafkaConfig.get(s).toString());
		}
		return properties;
	}

	def "variadic inputs works as expected"() {
		when: "build canvas"
		searchAndClick("MapModulesSpec")
		moduleShouldAppearOnCanvas("Stream")
		addAndWaitModule("Add")
		moveModuleBy("Add", 300, 100, 0, true)

		(1..20).each {
			connectEndpoints(findOutput("Stream", "value"), findInputByDisplayName("Add", "in$it"))
		}

		[3, 5, 7, 9, 11, 12, 13, 14, 15, 19].each {
			disconnectEndpoint(findInputByDisplayName("Add", "in$it"))
		}

		addAndWaitModule("Constant")
		setParameterValueForModule("Constant", "constant", "100")
		moveModuleBy("Constant", 50, 250, 0, true)

		(21..22).each {
			connectEndpoints(findOutput("Constant", "out"), findInputByDisplayName("Add", "in$it"))
		}

		addAndWaitModule("Label")
		moveModuleBy("Label", 500, 175, 0, true)
		connectEndpoints(findOutput("Add", "sum"), findInputByDisplayName("Label", "label"))

		and: "save and start real-time canvas"
		ensureRealtimeTabDisplayed()
		setCanvasName(getClass().simpleName + new Date().getTime())
		startCanvas(true)


		and: "data produced to Kafka"
		produceAllDataToKafka()

		then:
		waitFor(30) { $(".modulelabel").text().toDouble() == (33 * 10 + 100 * 2).toDouble() }

		cleanup:
		stopCanvasIfRunning()
	}

	private void produceAllDataToKafka() {
		produceToKafka("key-1", 30)
		produceToKafka("key-1", 40)
		produceToKafka("key-2", -50)
		produceToKafka("key-3", 115)
		produceToKafka("key-4", 0)
		produceToKafka("key-2", -15)
		produceToKafka("key-4", 1)
		produceToKafka("key-5", 0)
		produceToKafka("key-4", 33)
	}

	private void produceToKafka(String key, Double value) {
		kafka.sendJSON("pltRMd8rCfkij4mlZsQkJB", "", System.currentTimeMillis(),
			'{"key":' + key + ', "value": ' + value  + '}')
	}
}
