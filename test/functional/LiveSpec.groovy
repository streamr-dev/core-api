import grails.test.mixin.TestFor
import pages.*
import spock.lang.*

import com.unifina.controller.core.signalpath.LiveController
import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.service.BootService;
import com.unifina.utils.MapTraversal

import core.LoginTester1Spec
import core.mixins.CanvasMixin
import core.pages.LiveListPage
import core.pages.LiveShowPage


@Mixin(CanvasMixin)
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
			searchAndClickContains("LiveSpec")
			searchAndClickContains("label")
			def label = $(".modulename", text:"Label")
			connectEndpoints(findOutput("Stream", "rand"), findInput("Label", "label"))
			$("#runDropdown").click()
			waitFor { $("#runLiveModalButton").displayed }
			$("#runLiveModalButton").click()
		then: "launch live -modal opens"
			waitFor { $("#runLiveModal").displayed }
		
		when: "Name for live canvas is given and it is launched"
			$("#runLiveName") << liveName
			$("#runLiveButton").click()
		then: "LiveShowPage is opened"
			waitFor { at LiveShowPage }
			stopButton.displayed
			// Wait for data, sometimes takes more than 30sec to come
			waitFor(30){ $(".modulelabel").text() != "" }
			def oldLabel = $(".modulelabel").text()
			
		when: "Live canvas is stopped"
			stopButton.click()
			then: "The confirmation dialog is shown"
			waitFor { $(".modal-dialog").displayed }
		when: "Clicked OK"
			confirmOkButton.click()
		then: "The LiveShowPage is opened again, now with the start and delete -buttons"
			waitFor{ at LiveShowPage }
			startButton.displayed // TODO: FAILS, wait for stopping
			deleteButton.displayed
		
		when: "Started again"
			startButton.click()
		then: "The LiveShowPage is opened and data must change"
			waitFor { at LiveShowPage }
			waitFor(30){ $(".modulelabel").text() != oldLabel }
		
		when: "Went to the LiveListPage"
			to BuildPage
			to LiveListPage
		then: "The just created live canvas can be found"
			waitFor { at LiveListPage }
			$("table td", text:liveName).displayed
		
		when: "Clicked to open the just created live canvas"
			$("table td", text:liveName).click()
		then: "The LiveShowPage is opened"
			waitFor { at LiveShowPage }
			stopButton.displayed
		
		when: "Clicked to stop"
			stopButton.click()
		then: "The confirmation dialog is shown"
			waitFor { $(".modal-dialog").displayed }
		when: "Clicked OK"
			confirmOkButton.click()
		then: "The liveShowPage is opened again with the start and delete -buttons"
			waitFor{ at LiveShowPage }
			startButton.displayed
			deleteButton.displayed
			
		when: "Clicked to delete"
			deleteButton.click()
		then: "Confirmation dialog is opened"
			waitFor { $(".modal-dialog").displayed }
		when: "Clicked OK"
			confirmOkButton.click()
		then: "LiveListPage is opened, and the just created (and deleted) live canvas cannot be found from there anymore"
			waitFor{ at LiveListPage }
			waitFor { !($("table td", text:liveName).displayed) }
	}


}
