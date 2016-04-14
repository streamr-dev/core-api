import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.utils.MapTraversal
import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import grails.util.Holders

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

@Mixin(CanvasMixin)
@Mixin(ConfirmationMixin)
class MapModulesSpec extends LoginTester1Spec {

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

	void "countByKey counts key occurrences as expected"() {
		when: "build canvas"
		addAndConnectModules("CountByKey")

		and: "run canvas in realtime"
		ensureRealtimeTabDisplayed()
		setCanvasName(getClass().simpleName + new Date().getTime())
		startCanvas(true)

		and: "data produced to Kafka topic"
		produceAllDataToKafka()

		then: "Label for valueOfCurrentKey shows correct number"
		waitFor(10) { $(".modulelabel")[0].text() == "3.0" }

		and: "TableAsMap for map shows correct key-count pairs"
		tableContents() == ["key-1 2", "key-2 2", "key-3 1", "key-4 3", "key-5 1",] as Set

		cleanup:
		stopCanvasIfRunning()
	}

	void "sumByKey counts aggregated sums as expected"() {
		when:
		addAndConnectModules("SumByKey")
		connectEndpoints(findOutput("Stream", "value"), findInput("SumByKey", "value"))


		and: "run canvas in realtime"
		ensureRealtimeTabDisplayed()
		setCanvasName(getClass().simpleName + new Date().getTime())
		startCanvas(true)

		and: "data produced to Kafka topic"
		produceAllDataToKafka()

		then: "Label for valueOfCurrentKey shows correct number"
		waitFor(10) { $(".modulelabel")[0].text() == "34.0" }

		and: "TableAsMap for map shows correct key-count pairs"
		tableContents() == ["key-1 70", "key-2 -65", "key-3 115", "key-4 34", "key-5 0",] as Set

		cleanup:
		stopCanvasIfRunning()
	}

	void "forEach works as expected"() {
		String subCanvasName = "ForEach-SubCanvas-" + new Date().getTime()

		when: "build sub-canvas"
		addAndWaitModule("Multiply")
		moveModuleBy("Multiply", 150, 150)
		addAndWaitModule("Sum")
		moveModuleBy("Sum", 500, 150)
		addAndWaitModule("Max (window)")
		moveModuleBy("Max (window)", 150, 350)
		addAndWaitModule("LogNatural")
		moveModuleBy("LogNatural", 500, 350)

		connectEndpoints(findOutput("Max (window)", "out"), findInput("Multiply", "B"))
		connectEndpoints(findOutput("Max (window)", "out"), findInput("LogNatural", "in"))
		connectEndpoints(findOutput("Multiply", "A*B"), findInput("Sum", "in"))

		toggleExport("Sum", "out")
		toggleExport("Multiply", "A")
		toggleExport("Max (window)", "in")
		toggleExport("LogNatural", "out")


		then: "save sub canvas"
		setCanvasName(subCanvasName)
		saveCanvasAs(subCanvasName)

		when: "canvas cleared"
		waitFor { newButton.displayed }
		newButton.click()

		and: "build new canvas with ForEach module"
		addAndConnectModules("ForEach")

		addAndWaitModule("Label")
		moveModuleBy("Label", 650, 75, 1)

		chooseDropdownParameterForModule("ForEach", "canvas", subCanvasName)
		sleep(500)

		connectEndpoints(findOutput("Stream", "value"), findInput("ForEach", "A"))
		connectEndpoints(findOutput("Stream", "value"), findInput("ForEach", "in"))
		connectEndpoints(findOutput("ForEach", "out"), findInput("Label", "label", 0))
		connectEndpoints(findOutput("ForEach", "out2"), findInput("Label", "label", 1))

		and: "run canvas in realtime"
		ensureRealtimeTabDisplayed()
		setCanvasName(getClass().simpleName + new Date().getTime())
		startCanvas(true)

		and: "data produced to Kafka topic"
		produceAllDataToKafka()

		then: "Label for valueOfCurrentKey shows correct number"
		waitFor(10) { $(".modulelabel")[0].text() == (0 * 0 + 1 * 1 + 33 * 33).toString() + ".0" }

		$(".modulelabel")[1].text() == new DecimalFormat("#.########",
			DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(Math.log(33))

		// key-1 2500, log(40) = 3.688879
		// key-2 2725, log(-15)
		// key-3 13225, log(115) = 4.744932
		// key-4 1090, log(33) = 3.496508
		// key-5 0, log(0)  = - inf
		and: "TableAsMap for map shows correct key-count pairs"
		tableContents() == [
			'key-1 {"out2":3.68887945,"out":2500}',
			'key-2 {"out":2725}',
			'key-3 {"out2":4.74493213,"out":13225}',
			'key-4 {"out2":3.49650756,"out":1090}',
			'key-5 {"out":0}'
		] as Set

		cleanup:
		stopCanvasIfRunning()
	}

	private void addAndConnectModules(String moduleName) {
		searchAndClick("MapModulesSpec")
		moduleShouldAppearOnCanvas("Stream")
		addAndWaitModule(moduleName)
		moveModuleBy(moduleName, 200, 200, 0, true)
		addAndWaitModule("MapAsTable")
		moveModuleBy("MapAsTable", 650, 400)
		addAndWaitModule("Label")
		moduleShouldAppearOnCanvas("Label")
		moveModuleBy("Label", 650, 200)

		connectEndpoints(findOutput("Stream", "key"), findInput(moduleName, "key"))
		connectEndpoints(findOutput(moduleName, "map"), findInput("MapAsTable", "map"))
		if (moduleName != "ForEach") {
			connectEndpoints(findOutput(moduleName, "valueOfCurrentKey"), findInput("Label", "label", 0))
		}
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

	private Set<String> tableContents() {
		def mapAsTable = findModuleOnCanvas("MapAsTable")
		return mapAsTable.find(".event-table-module-content tr").collect { it.text() }.toSet()
	}

	private void produceToKafka(String key, Double value) {
		kafka.sendJSON("pltRMd8rCfkij4mlZsQkJB", "", System.currentTimeMillis(),
			'{"key":' + key + ', "value": ' + value  + '}')
	}

	private def makeKafkaConfiguration() {
		Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(Holders.config, "unifina.kafka"));
		Properties properties = new Properties();
		for (String s : kafkaConfig.keySet()) {
			properties.setProperty(s, kafkaConfig.get(s).toString());
		}
		return properties;
	}
}
