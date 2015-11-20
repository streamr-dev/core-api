import grails.test.mixin.TestFor
import spock.lang.*

import com.unifina.controller.core.signalpath.LiveController
import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.service.BootService
import com.unifina.utils.MapTraversal

import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.mixins.ConfirmationMixin
import core.pages.LiveListPage
import core.pages.LiveShowPage


@Mixin(CanvasMixin)
@Mixin(ConfirmationMixin)
@TestFor(LiveController) // This makes grailsApplication available
public class LiveSpec extends LoginTester1Spec {

	static Timer timer
	static UnifinaKafkaProducer kafka
	
	def setupSpec() {
		BootService.mergeDefaultConfig(grailsApplication)
		Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(grailsApplication.config, "unifina.kafka"));
		Properties properties = new Properties();
		for (String s : kafkaConfig.keySet())
			properties.setProperty(s, kafkaConfig.get(s).toString());
		
		kafka = new UnifinaKafkaProducer(properties)
		
		final TimerTask task = new TimerTask() {
			void run() {
				synchronized(kafka) {
					kafka.sendJSON("RUj6iJggS3iEKsUx5C07Ig", "", System.currentTimeMillis(), '{"rand":'+Math.random()+'}')
				}
			}
		}
		
		// Produce to a live feed
		timer = new Timer()
		timer.schedule(task, 1000L, 1000L)
	}
	
	def cleanupSpec() {
		timer.cancel()
		synchronized(kafka) {
			kafka.close()
		}
	}
	
	def "launching, modifying and deleting live canvas works correctly"() {
		// Unique name for the live
		String liveName = "test" + new Date().getTime()
		
		when: "Modules are added and clicked 'Launch live'"
			// The stream
			searchAndClick("LiveSpec")
			moduleShouldAppearOnCanvas("Stream")
			searchAndClick("Label")
			moduleShouldAppearOnCanvas("Label")
			interact {
				clickAndHold(findModuleOnCanvas("Label"))
				moveByOffset(0, 200)
			}
			
			connectEndpoints(findOutput("Stream", "rand"), findInput("Label", "label"))
			
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
			
			// Wait for data, sometimes takes more than 30sec to come
			waitFor(30){ $(".modulelabel").text() != "" }
			def oldLabel = $(".modulelabel").text()
			
		when: "Help button is clicked"
			findModuleOnCanvas("Label").find(".modulebutton .help").click()
		then: "Dialog is opened with webcomponent tag shown"
			waitFor {
				$(".modal-dialog .modulehelp", text:contains("streamr-label"))
			}
			
		when: "Help dialog close button is clicked"
			$(".modal-dialog button.close").click()
		then: "Dialog exits"
			waitFor {
				$(".modal-dialog").size()==0
			}	
		
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
			waitFor(30){ $(".modulelabel").text() != oldLabel }
		
		when: "Went to the LiveListPage"
			to LiveListPage
		then: "The just created live canvas can be found"
			waitFor { at LiveListPage }
			$(".table .td", text:liveName).displayed
		
		when: "Clicking to open the just created live canvas"
			$(".table .td", text:liveName).click()
		then: "The LiveShowPage is opened"
			waitFor { at LiveShowPage }
			stopButton.displayed
		
		when: "Clicked to stop"
			stopButton.click()
		then: "The confirmation dialog is shown"
			waitForConfirmation()
		when: "Clicked OK"
			acceptConfirmation()
		then: "The liveShowPage is opened again with the start and delete -buttons, no error must be shown"
			waitFor{ at LiveShowPage }
			startButton.displayed
			deleteButton.displayed
			$(".alert.alert-info").displayed
			!$(".alert.alert-danger").displayed
			
		when: "Clicked to delete"
			deleteButton.click()
		then: "Confirmation dialog is opened"
			waitForConfirmation()
		when: "Clicked OK"
			acceptConfirmation()
		then: "LiveListPage is opened, and the just created (and deleted) live canvas is not displayed anymore"
			waitFor{ at LiveListPage }
			waitFor { !($(".table .td", text:liveName).displayed) }
	}
	
	def "an alert must be shown if running canvas cannot be pinged"() {
		to LiveListPage
		waitFor{ at LiveListPage }
		
		when: "selecting running canvas"
			$(".table .td", text:"LiveSpec dead").click()
		then: "navigate to show page that shows an error"
			waitFor {at LiveShowPage}
			waitFor {$(".alert.alert-danger").displayed}
	}
	
	def "don't subscribe to stopped SignalPath channels"() {
		to LiveListPage
		waitFor{ at LiveListPage }
		
		when: "selecting running canvas"
			$(".table .td", text:"LiveSpec stopped").click()
		then: "navigate to show page"
			waitFor {at LiveShowPage}
			!js.exec("return SignalPath.getConnection().isConnected()")
	}

	def "stopping non-running signalpaths must mark them as stopped and show a flash message"() {
		to LiveListPage
		waitFor{ at LiveListPage }
		
		when: "selecting running canvas"
			$(".table .td", text:"LiveSpec dead").click()
		then: "navigate to show page that shows an error"
			waitFor {at LiveShowPage}
		
		when: "stop button is clicked"
			stopButton.click()
		then: "confirmation is shown"
			waitFor { $(".modal-dialog").displayed } // WHY does waitForConfirmation() from ConfirmationMixin give groovy.lang.MissingMethodException here??!
			
		when: "confirmation accepted"
			$(".modal-dialog .btn-primary").click() // WHY does acceptConfirmation() from ConfirmationMixin give groovy.lang.MissingMethodException here??!
		then: "must show alert, start button and delete button"
			waitFor{ at LiveShowPage }
			$(".alert.alert-danger").displayed
			startButton.displayed
			deleteButton.displayed
			
		when: "canvas is resumed"
			startButton.click()
		then: "info alert and stop button must be displayed"
			waitFor{ at LiveShowPage }
			$(".alert.alert-info").displayed
			!startButton.displayed
			!deleteButton.displayed
			stopButton.displayed
	}

}
