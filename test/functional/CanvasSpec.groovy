
import com.unifina.kafkaclient.UnifinaKafkaProducer

import core.LoginTester1Spec
import core.mixins.CanvasMixin

class CanvasSpec extends LoginTester1Spec {
	
	def setupSpec(){
		CanvasSpec.metaClass.mixin(CanvasMixin)
	}

	def "clicking module close button in canvas should remove it"() {
		setup: "Barify is added via module browser"
			addAndWaitModule 'Barify'
			def div = findModuleOnCanvas 'Barify'
			waitFor {
				div.find('.delete').displayed
			}
		when: "I click on the close button"
			div.find('.delete').click()
		then: "module should be removed from canvas"
			!findModuleOnCanvas('Barify')
	}
	def "drag and dropping a module to canvas should add it"() {
		when: "Barify is added via drag and drop"
			dragAndDropModule 'Barify'
		then: "module should appear on canvas"
			moduleShouldAppearOnCanvas 'Barify'
	}

	def "adding Barify to canvas should add it"() {
		when: "Barify is added via module browser"
			addModule 'Barify'
		then: "module should appear on canvas"
			moduleShouldAppearOnCanvas 'Barify'
	}


	def "adding a module through search box should add it"() {
		when: "Barify is added via search"
			searchAndClick 'Barify'
		then: "module should appear on canvas"
			moduleShouldAppearOnCanvas 'Barify'
	}

	def "adding a stream through search box should add it"() {
		when: "CanvasSpec stream is added via search"
			searchAndClick 'CanvasSpec'
		then: "stream should appear on canvas"
			moduleShouldAppearOnCanvas 'Stream'
	}

	def "searching for module with alternate name should show the module in results"() {
		when: "Plus is searched"
			search << 'plus'
		then: "Add is shown in results"
			waitFor {
				searchControl.find('.tt-suggestion p', text: contains("Add"))
			}
	}
	
	def "searching for stream with its description should show the stream in results"() {
		when: "a search is entered"
			search << 'to test running canvases'
		then: "CanvasSpec is shown in results"
			waitFor {
				searchControl.find('.tt-suggestion p', text: contains("CanvasSpec"))
				searchControl.find('.tt-suggestion p', text: contains("to test running canvases"))
			}
	}
	
	def "clicking a canvas in the load browser should load the signalpath"() {
		when: "load button is clicked"
			loadSignalPath '1'
		then: "signalpath content must be loaded"
			waitFor {
				$("#module_2")
			}
	}
	
	def "unsaved canvases should show the save as option"() {
		when: "save dropdown button is clicked"
			saveDropdownButton.click()
		then: "save as button should be shown"
			saveAsButton.displayed
		then: "save in place button should not be shown"
			!saveButton.displayed
	}
	
	def "saved canvases should show the save in place option"() {
		when: "load button is clicked"
			loadSignalPath '1'
		then: "signalpath content must be loaded"
			waitFor {
				$("#module_2")
			}
		when: "save dropdown button is clicked"
			saveDropdownButton.click()
		then: "save as button should be shown"
			saveAsButton.displayed
		then: "save in place button should be shown"
			saveButton.displayed
	}
	
	def "begin- and end date datepickers"() {
		when: "a signalpath is loaded"
			loadSignalPath("1")
		then: "begin date and end date are loaded"
			waitFor {
				beginDate.value() == "2015-07-02"
				endDate.value() == "2015-07-03"
			}
		
		when: "the begin date field is clicked"
			beginDate.click()
		then: "a datepicker is displayed that shows the current date"
			$(".datepicker").displayed
			$(".datepicker .active.day").text() == "2"
		
		when: "a date is selected in the datepicker"
			$(".datepicker .active.day").parent().find(".day", text:"3").click()
		then: "the datepicker is closed"
			$(".datepicker").size() == 0
		then: "the input field shows the selected value"
			beginDate.value() == "2015-07-03"
	}
	
	private void sleepForNSeconds(int n) {
		def originalMilliseconds = System.currentTimeMillis()
		waitFor(n + 1, 0.5) {
			(System.currentTimeMillis() - originalMilliseconds) > (n * 1000)
		}
	}

	def "running a SignalPath should produce output"() {
		when: "SignalPath is loaded"
			loadSignalPath 'test-run-canvas'
		then: "signalpath content must be loaded"
			moduleShouldAppearOnCanvas('Table')
			
		when: "run button is clicked"
			runButton.click()
		then: "output should be produced"
			waitFor(30) {
				$('#run', text: contains('Abort'))
				$('.modulebody .table td', text: "2015-02-23 18:30:00.011")
			}
			
		when: "abort button is clicked"
			$('#run', text: contains('Abort')).click()
			sleepForNSeconds(2) // Allow some time for server-side stuff to clean up
		then: "button must change back to run"
			waitFor {
				$('#run', text: 'Run')
			}
	}
	
	def "running a SignalPath on current day should read from Kafka"() {
		def myMessage = "hello "+new Date()
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
		
		when: "SignalPath is loaded"
			loadSignalPath 'test-run-canvas'
		then: "signalpath content must be loaded"
			moduleShouldAppearOnCanvas('Table')
			
		when: "data is produced and signalpath is run on current date"
			UnifinaKafkaProducer kafka = new UnifinaKafkaProducer("192.168.10.21:9092", "192.168.10.21:2181")
			// Procuce to the stream that test-run-canvas reads from
			kafka.sendJSON("c1_fiG6PTxmtnCYGU-mKuQ", "", System.currentTimeMillis(), '{"myMsg":"'+myMessage+'"}')
			beginDate = df.format(new Date())
			endDate = df.format(new Date())
			runButton.click()
		then: "output should be produced"
			waitFor(30) {
				$('.modulebody .table td', text: myMessage)
			}
	}

}
