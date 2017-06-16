import com.unifina.domain.data.Stream
import com.unifina.service.StreamService
import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.mixins.StreamMixin
import spock.lang.Shared

@Mixin(CanvasMixin)
@Mixin(ConfirmationMixin)
@Mixin(StreamMixin)
class VariadicEndpointsSpec extends LoginTester1Spec {

	@Shared Stream testStream = new Stream()
	@Shared StreamService streamService

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		this.class.metaClass.mixin(CanvasMixin)
		this.class.metaClass.mixin(ConfirmationMixin)
		this.class.metaClass.mixin(StreamMixin)

		streamService = createStreamService()
		testStream.id = "pltRMd8rCfkij4mlZsQkJB"
	}

	def cleanupSpec() {
		cleanupStreamService(streamService)
	}

	def "variadic inputs works as expected"() {
		when: "build canvas"
		searchAndClick("MapModulesSpec")
		moduleShouldAppearOnCanvas("Stream")
		addAndWaitModule("Add")
		moveModuleBy("Add", 300, 100, 0, true)

		(1..20).each {
			connectEndpoints(findOutput("Stream", "value"), findInputByDisplayName("Add", "in$it"))
			if (it > 2) {
				renameEndpoint("Add", "value", "in$it") // Rename after auto-rename
			}
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
		produceAllDataToStream()

		then:
		waitFor(30) { $(".modulelabel").text().toDouble() == (33 * 10 + 100 * 2).toDouble() }

		cleanup:
		stopCanvasIfRunning()
	}

	def "variadic outputs works as expected"() {
		when: "build canvas"
		searchAndClick("MapModulesSpec")
		moduleShouldAppearOnCanvas("Stream")

		addAndWaitModule("NewMap")
		moveModuleBy("NewMap", 0, 200, 0, true)
		chooseDropdownParameterForModule("NewMap", "alwaysNew", "false")

		addAndWaitModule("PutToMap")
		moveModuleBy("PutToMap", 300, 150, 0, true)

		addAndWaitModule("GetMultiFromMap")
		moveModuleBy("GetMultiFromMap", 550, 175, 0, true)

		addAndWaitModule("Label")
		moveModuleBy("Label", 750, 50, 0, true)

		addAndWaitModule("Label")
		moveModuleBy("Label", 750, 150, 1, true)

		addAndWaitModule("Label")
		moveModuleBy("Label", 750, 250, 2, true)

		addAndWaitModule("Label")
		moveModuleBy("Label", 750, 350, 3, true)

		addAndWaitModule("Label")
		moveModuleBy("Label", 750, 450, 4, true)

		connectEndpoints(findOutput("NewMap", "out"), findInputByDisplayName("PutToMap", "map"))
		connectEndpoints(findOutput("Stream", "key"), findInputByDisplayName("PutToMap", "key"))
		connectEndpoints(findOutput("Stream", "value"), findInputByDisplayName("PutToMap", "value"))
		connectEndpoints(findOutput("PutToMap", "map"), findInputByDisplayName("GetMultiFromMap", "in"))

		connectEndpoints(findOutputByDisplayName("GetMultiFromMap", "out1"), findInput("Label", "label", 0))
		connectEndpoints(findOutputByDisplayName("GetMultiFromMap", "out2"), findInput("Label", "label", 1))
		renameEndpoint("GetMultiFromMap", "out3", "key-2")
		connectEndpoints(findOutputByDisplayName("GetMultiFromMap", "key-2"), findInput("Label", "label", 2))
		connectEndpoints(findOutputByDisplayName("GetMultiFromMap", "out4"), findInput("Label", "label", 3))
		connectEndpoints(findOutputByDisplayName("GetMultiFromMap", "out5"), findInput("Label", "label", 4))
		disconnectEndpoint(findOutputByDisplayName("GetMultiFromMap", "out1"))
		connectEndpoints(findOutputByDisplayName("GetMultiFromMap", "out6"), findInput("Label", "label", 0))
		connectEndpoints(findOutputByDisplayName("Stream", "key"), findInput("NewMap", "trigger"))

		renameEndpoint("GetMultiFromMap", "out2", "key-1")
		renameEndpoint("GetMultiFromMap", "out4", "key-5")
		renameEndpoint("GetMultiFromMap", "out5", "nonexistent")
		renameEndpoint("GetMultiFromMap", "out6", "key-3")

		and: "save and start real-time canvas"
		ensureRealtimeTabDisplayed()
		setCanvasName(getClass().simpleName + new Date().getTime())
		startCanvas(true)


		and: "data produced to Kafka"
		produceAllDataToStream()

 		then:
		waitFor(30) { $(".modulelabel")[3].text().toDouble() == 0d }
		$(".modulelabel")[0].text().toDouble() == 115d
		$(".modulelabel")[1].text().toDouble() == 40d
		$(".modulelabel")[2].text().toDouble() == -15d
		$(".modulelabel")[4].text() == ""

		cleanup:
		stopCanvasIfRunning()
	}

	def "variadic input-output pair works as expected"() {
		when: "build canvas"
		searchAndClick("MapModulesSpec")
		moduleShouldAppearOnCanvas("Stream")

		addAndWaitModule("Constant")
		moveModuleBy("Constant", 0, 150, 0, true)

		addAndWaitModule("GreaterThan")
		moveModuleBy("GreaterThan", 300, 150, 0, true)

		addAndWaitModule("TextLength")
		moveModuleBy("TextLength", 300, 0, 0, true)

		addAndWaitModule("Filter")
		moveModuleBy("Filter", 550, 175, 0, true)

		connectEndpoints(findOutput("Stream", "value"), findInput("GreaterThan", "A"))
		connectEndpoints(findOutput("Constant", "out"), findInput("GreaterThan", "B"))
		connectEndpoints(findOutput("GreaterThan", "A&gt;B"), findInput("Filter", "pass"))

		connectEndpoints(findOutput("Stream", "value"), findInputByDisplayName("Filter", "in1"))
		renameEndpoint("Filter", "value", "in1") // Rename after auto-rename
		renameEndpoint("Filter", "value", "out1") // Rename after auto-rename

		connectEndpoints(findOutput("Stream", "key"), findInput("TextLength", "text"))
		connectEndpoints(findOutput("TextLength", "length"), findInputByDisplayName("Filter", "in2"))
		renameEndpoint("Filter", "length", "in2") // Rename after auto-rename
		renameEndpoint("Filter", "length", "out2") // Rename after auto-rename

		connectEndpoints(findOutput("Stream", "value"), findInputByDisplayName("Filter", "in3"))
		renameEndpoint("Filter", "value", "in3") // Rename after auto-rename
		renameEndpoint("Filter", "value", "out3") // Rename after auto-rename

		addAndWaitModule("Label")
		moveModuleBy("Label", 0, 500, 0, true)

		addAndWaitModule("Label")
		moveModuleBy("Label", 200, 500, 1, true)

		addAndWaitModule("Label")
		moveModuleBy("Label", 400, 500, 2, true)

		addAndWaitModule("Label")
		moveModuleBy("Label", 600, 500, 3, true)

		connectEndpoints(findOutputByDisplayName("Filter", "out4"), findInput("Label", "label", 3))
		connectEndpoints(findOutputByDisplayName("Filter", "out2"), findInput("Label", "label", 2))
		connectEndpoints(findOutputByDisplayName("Filter", "out1"), findInput("Label", "label", 1))
		connectEndpoints(findOutputByDisplayName("Filter", "out3"), findInput("Label", "label", 0))

		disconnectEndpoint(findInput("Label", "label", 2))

		disconnectEndpoint(findInputByDisplayName("Filter", "in1"))
		disconnectEndpoint(findInputByDisplayName("Filter", "in3"))
		disconnectEndpoint(findInputByDisplayName("Filter", "in2"))

		connectEndpoints(findOutputByDisplayName("TextLength", "length"), findInputByDisplayName("Filter", "in4"))
		renameEndpoint("Filter", "length", "in4") // Rename after auto-rename
		renameEndpoint("Filter", "length", "out4") // Rename after auto-rename

		connectEndpoints(findOutputByDisplayName("Stream", "value"), findInputByDisplayName("Filter", "in5"))
		renameEndpoint("Filter", "value", "in5") // Rename after auto-rename
		renameEndpoint("Filter", "value", "out5") // Rename after auto-rename

		connectEndpoints(findOutputByDisplayName("Stream", "value"), findInputByDisplayName("Filter", "in6"))
		renameEndpoint("Filter", "value", "in6") // Rename after auto-rename
		renameEndpoint("Filter", "value", "out6") // Rename after auto-rename

		connectEndpoints(findOutputByDisplayName("Filter", "out5"), findInput("Label", "label", 0))
		connectEndpoints(findOutputByDisplayName("Filter", "out6"), findInput("Label", "label", 1))

		and: "save and start real-time canvas"
		ensureRealtimeTabDisplayed()
		setCanvasName(getClass().simpleName + new Date().getTime())
		startCanvas(true)


		and: "data produced to Kafka"
		produceAllDataToStream()

		then:
		waitFor(30) { $(".modulelabel")[0].text().toDouble() == 33d }
		$(".modulelabel")[1].text().toDouble() == 33d
		$(".modulelabel")[2].text() == ""
		$(".modulelabel")[3].text().toDouble() == 5d

		cleanup:
		stopCanvasIfRunning()
	}

	private void produceAllDataToStream() {
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

	private void produceToStream(String key, Double value) {
		streamService.sendMessage(testStream, [key: key, value: value], 30)
	}
}
