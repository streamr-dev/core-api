import com.unifina.domain.data.Stream
import com.unifina.service.StreamService
import LoginTester1Spec
import mixins.CanvasMixin
import mixins.ConfirmationMixin
import mixins.StreamMixin
import org.apache.log4j.Logger
import spock.lang.Shared

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.regex.Pattern

class MapModulesSpec extends LoginTester1Spec implements CanvasMixin, ConfirmationMixin, StreamMixin {

	@Shared Logger log = Logger.getLogger(MapModulesSpec)
	@Shared Stream testStream
	@Shared StreamService streamService

	def setupSpec() {
		testStream = new Stream()
		testStream.id = "pltRMd8rCfkij4mlZsQkJB"
		streamService = createStreamService()
	}

	def cleanupSpec() {
		cleanupStreamService(streamService)
	}

	void "countByKey counts key occurrences as expected"() {
		when: "build canvas"
		addAndConnectModules("CountByKey")

		and: "run canvas in realtime"
		ensureRealtimeTabDisplayed()
		setCanvasName(getClass().simpleName + new Date().getTime())
		startCanvas(true)

		and: "data produced to Kafka topic"
		produceAllDataToStream()

		then: "TableAsMap for map shows correct key-count pairs"
		tableContains(["key-1 2", "key-2 2", "key-3 1", "key-4 3", "key-5 1"])

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
		produceAllDataToStream()

		then: "TableAsMap for map shows correct key-count pairs"
		tableContains(["key-1 70", "key-2 -65", "key-3 115", "key-4 34", "key-5 0"])

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
		setParameterValueForModule("Sum", "windowLength", "5")
		chooseDropdownParameterForModule("Sum", "windowType", "seconds")
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
		moveModuleBy("Label", 650, 200)
		addAndWaitModule("Label")
		moveModuleBy("Label", 650, 75, 1)

		chooseDropdownParameterForModule("ForEach", "canvas", subCanvasName)
		sleep(1000)

		connectEndpoints(findOutput("Stream", "value"), findInput("ForEach", "A"))
		sleep(1000)
		connectEndpoints(findOutput("Stream", "value"), findInputByDisplayName("ForEach", "in"))
		sleep(1000)
		connectEndpoints(findOutputByDisplayName("ForEach", "out"), findInput("Label", "label", 0))
		sleep(1000)
		connectEndpoints(findOutputByDisplayName("ForEach", "out2"), findInput("Label", "label", 1))

		and: "run canvas in realtime"
		ensureRealtimeTabDisplayed()
		setCanvasName(getClass().simpleName + new Date().getTime())
		startCanvas(true)

		and: "data produced to Kafka topic"
		produceAllDataToStream()

		then:
		waitFor {
			$(".modulelabel")[1].text() == new DecimalFormat("#.########",
					DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(Math.log(33))
		}

		// key-1 2500, log(40) = 3.688879
		// key-2 2725, log(-15)
		// key-3 13225, log(115) = 4.744932
		// key-4 1090, log(33) = 3.496508
		// key-5 0, log(0)  = - inf
		// \\d* eliminates float inaccuracy by matching "some number of digits", e.g. 0000005
		and: "TableAsMap for map shows correct key-count pairs"
		waitFor {
			tableContains([
					'key-1 ."out2":3.688879\\d*,"out":2500.',
					'key-2 ."out":2725.',
					'key-3 ."out2":4.744932\\d*,"out":13225.',
					'key-4 ."out2":3.496507\\d*,"out":1090.',
					'key-5 ."out":0.'
			])
		}

		and: "After sum values have fallen from the window, sum outputs must show zero"
		waitFor(20) {
			$(".modulelabel")[0].text() == "0.0"
			tableContains([
					'key-1 ."out2":3.688879\\d*,"out":0.',
					'key-2 ."out":0.',
					'key-3 ."out2":4.744932\\d*,"out":0.',
					'key-4 ."out2":3.496507\\d*,"out":0.',
					'key-5 ."out":0.'
			])
		}

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

		connectEndpoints(findOutput("Stream", "key"), findInput(moduleName, "key"))
		connectEndpoints(findOutput(moduleName, "map"), findInput("MapAsTable", "map"))
	}

	private void produceAllDataToStream() {
		Thread.sleep(2000)
		produceToStream("key-1", 30)
		produceToStream("key-1", 40)
		produceToStream("key-2", -50)
		produceToStream("key-3", 115)
		produceToStream("key-4", 0)
		produceToStream("key-2", -15)
		produceToStream("key-4", 1)
		produceToStream("key-5", 0)
		produceToStream("key-4", 33)
	}

	private boolean tableContains(Collection<String> patterns) {
		def mapAsTable = findModuleOnCanvas("MapAsTable")
		waitFor {
			patterns.every {
				def results = mapAsTable.find(".event-table-module-content tbody tr", text: Pattern.compile(".*${it}.*"))
				if (results.isEmpty()) {
					log.info("Could not find $it")
				}
				return !results.isEmpty()
			}
		}
	}

	private void produceToStream(String key, Double value) {
		streamService.sendMessage(testStream, [key: key, value: value], 30)
	}
}
