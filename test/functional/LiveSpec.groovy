import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.protocol.message_layer.StreamMessageV30
import com.unifina.domain.data.Stream
import com.unifina.service.StreamService
import LoginTester1Spec
import mixins.CanvasMixin
import mixins.ConfirmationMixin
import mixins.StreamMixin
import pages.CanvasListPage
import pages.CanvasPage
import spock.lang.Shared

class LiveSpec extends LoginTester1Spec implements CanvasMixin, ConfirmationMixin, StreamMixin {

	static Timer timer

	@Shared StreamService streamService

	def setupSpec() {
		final Stream testStream = new Stream()
		testStream.id = "RUj6iJggS3iEKsUx5C07Ig"

		final StreamService ss = streamService = createStreamService()

		final TimerTask task = new TimerTask() {
			void run() {
				StreamMessage msg = new StreamMessageV30(testStream.id, 0, 30L, 0L,
					"", "", null, 0L, StreamMessage.ContentType.CONTENT_TYPE_JSON,
					[rand: Math.random()], StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
				ss.sendMessage(msg)
			}
		}

		// Produce to a live feed
		timer = new Timer()
		timer.schedule(task, 1000L, 1000L)
	}

	def cleanupSpec() {
		timer.cancel()
		cleanupStreamService(streamService)
	}

	def "launching, modifying and deleting live canvas works correctly"() {
		// Unique name for the live
		String liveName = "test" + new Date().getTime()

		when: "Modules are added and 'Launch live' clicked"
			// The stream
			searchAndClick("LiveSpec")
			moduleShouldAppearOnCanvas("Stream")
			searchAndClick("Label")
			moduleShouldAppearOnCanvas("Label")
			moveModuleBy("Label", 200, 200)

			connectEndpoints(findOutput("Stream", "rand"), findInput("Label", "label"))

			setCanvasName(liveName)
			ensureRealtimeTabDisplayed()
			startCanvas(true)
			noNotificationsVisible()
		then: "Some data should appear"
			// Wait for data, sometimes takes more than 30sec to come
			waitFor(30){ $(".modulelabel").text() != "" }
			def oldLabel = $(".modulelabel").text()

		when: "Help button is clicked"
			findModuleOnCanvas("Label").find(".modulebutton .help").click()
		then: "Dialog is opened with webcomponent tag shown"
			waitFor {
				$(".module-help-dialog .modulehelp", text:contains("streamr-label"))
			}

		when: "Help dialog close button is clicked"
			$(".module-help-dialog button.close").click()
		then: "Dialog exits"
			waitFor {
				$(".module-help-dialog").size()==0
			}

		when: "Stop button is clicked"
			stopCanvas()
		and: "Starting again"
			ensureRealtimeTabDisplayed()
			startCanvas(true)

		then: "Data must change"
			waitFor(30){ $(".modulelabel").text() != oldLabel }

		when: "Going to CanvasListPage"
			to CanvasListPage
		then: "The just created canvas can be found"
			waitFor { at CanvasListPage }
			$(".table .td", text:liveName).displayed

		when: "The canvas is clicked"
			$(".table .td", text:liveName).click()
		then: "The CanvasPage is opened"
			waitFor { at CanvasPage }
		then: "The canvas is loaded"
			waitFor { findModuleOnCanvas("Label") }
		then: "The button is in running state"
			waitFor { runRealtimeButton.text().contains("Stop") }

		// TODO: test canvas delete functionality once it's implemented

		cleanup:
			stopCanvasIfRunning()
	}

	def "an alert must be shown if running canvas cannot be pinged"() {
		to CanvasListPage
		waitFor{ at CanvasListPage }

		when: "selecting running canvas"
			$(".table .td", text:"LiveSpec dead").click()
		then: "navigate to show page that shows an error"
			waitFor {at CanvasPage}
			waitFor(20) {$(".alert.alert-danger").displayed}
	}

	def "don't subscribe to stopped SignalPath channels"() {
		to CanvasListPage
		waitFor{ at CanvasListPage }

		when: "selecting running canvas"
			$(".table .td", text:"LiveSpec stopped").click()
		then: "connection must not be connected"
			waitFor {at CanvasPage}
			waitFor {findModuleOnCanvas("Label")}
			!js.exec("return SignalPath.getConnection().isConnected()")
	}

	def "stopping non-running signalpaths must mark them as stopped and show a notification"() {
		to CanvasListPage
		waitFor{ at CanvasListPage }

		when: "selecting a dead canvas"
			$(".table .td", text:"LiveSpec dead").click()
		then: "navigate to editor page with correct run button state"
			waitFor(20) {
				at CanvasPage
				runRealtimeButton.text().contains("Stop")
			}

		when: "stop button is clicked"
			noNotificationsVisible()
			runRealtimeButton.click()
		then: "confirmation is shown"
			waitForConfirmation(".stop-confirmation-dialog")

		when: "confirmation accepted"
			acceptConfirmation(".stop-confirmation-dialog")
		then: "must show alert and start button"
			waitFor(20) {
				$(".alert.alert-danger").displayed
				runRealtimeButton.text().contains("Start")
			}

		when: "canvas is resumed"
			noNotificationsVisible()
			runRealtimeButton.click()
		then: "info alert and stop button must be displayed"
			waitFor { $(".alert.alert-success").displayed }
	}

	def "producing realtime data to other streams"() {
		loadSignalPath("LiveSpec-SendToStream")
		ensureRealtimeTabDisplayed()
		stopCanvasIfRunning()

		when:
		resetAndStartCanvas(true)

		then:
		waitFor {
			findModuleOnCanvas("Table", 0).find('.event-table-module-content tbody tr').size() > 0
		}
		waitFor {
			findModuleOnCanvas("Table", 1).find('.event-table-module-content tbody tr').size() > 0
		}
	}

}
