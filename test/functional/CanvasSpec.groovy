import core.mixins.KafkaMixin
import core.mixins.ListPageMixin
import core.pages.CanvasListPage
import core.pages.CanvasPage
import org.openqa.selenium.Keys

import java.text.SimpleDateFormat

import com.unifina.kafkaclient.UnifinaKafkaProducer

import core.LoginTester1Spec
import core.mixins.CanvasMixin

class CanvasSpec extends LoginTester1Spec {
	
	def setupSpec(){
		CanvasSpec.metaClass.mixin(CanvasMixin)
		CanvasSpec.metaClass.mixin(ListPageMixin)
		CanvasSpec.metaClass.mixin(KafkaMixin)
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
			search.click()
		then: "Add is shown in results"
			waitFor {
				$('.streamr-search-menu .streamr-search-suggestion p', text: contains("Add")).displayed
			}
	}

	def "searching for stream with its description should show the stream in results"() {
		when: "a search is entered"
			search << 'to test running canvases'
			search.click()
		then: "CanvasSpec is shown in results"
			waitFor {
				$('.streamr-search-menu .streamr-search-suggestion p', text: contains("CanvasSpec"))
				$('.streamr-search-menu .streamr-search-suggestion p', text: contains("to test running canvases"))
			}
	}

	def "clicking a canvas in the load browser should load the signalpath"() {
		when: "load button is clicked"
			loadSignalPath 'CanvasSpec test loading a SignalPath'
		then: "signalpath content must be loaded"
			waitFor {
				findModuleOnCanvas("Add")
			}
	}

	def "canvas can be saved"() {
		def name = "CanvasSpec-" + System.currentTimeMillis()
		setup:
			addAndWaitModule("Label")
		when: "saved with name"
			saveCanvasAs(name)
			newButton.click()
		then: "canvas can be loaded"
			loadSignalPath(name)
			findModuleOnCanvas("Label")
	}

	def "url in address bar reflects editor canvas"() {
		def name = "CanvasSpec-" + System.currentTimeMillis()

		expect: "the url is clear"
			waitFor { driver.currentUrl.endsWith("/canvas/editor") }

		when: "saved with name"
			saveCanvasAs(name)
		then: "url changes"
			waitFor { !driver.currentUrl.endsWith("/canvas/editor") }
			at CanvasPage

		when: "click new button"
			newButton.click()
		then: "url is clear"
			waitFor { driver.currentUrl.endsWith("/canvas/editor") }

		when: "canvas is loaded"
			loadSignalPath(name)
		then: "the url has changed"
			waitFor { !driver.currentUrl.endsWith("/canvas/editor") }

	}

	def "going back after creating a new canvas reloads the last one"() {
		def name = "CanvasSpec-" + System.currentTimeMillis()
		when: "saved with name"
			addAndWaitModule("Table")
			saveCanvasAs(name)
			newButton.click()
		then: "no module"
			waitFor {
				at CanvasPage
				!findModuleOnCanvas("Table")
			}
		when: "canvas is loaded"
			loadSignalPath(name)
		then: "module can be found"
			findModuleOnCanvas("Table")
		when: "new canvas is created"
			newButton.click()
		then: "no module"
			waitFor { !findModuleOnCanvas("Table") }
		when: "clicked back button"
			driver.navigate().back()
		then: "module can be found"
			waitFor {
				findModuleOnCanvas("Table")
			}
	}

	def "going back after loading a canvas reloads the last one"() {
		setup:
			def name = "CanvasSpec-" + System.currentTimeMillis()
			addAndWaitModule("Table")
			saveCanvasAs(name)
			newButton.click()
			def name2 = "CanvasSpec-" + System.currentTimeMillis()
			addAndWaitModule("Label")
			saveCanvasAs(name2)
			newButton.click()
		when: "canvas 1 is loaded"
			loadSignalPath(name)
		then: "Table can be found, no Label"
			waitFor {
				findModuleOnCanvas("Table")
				!findModuleOnCanvas("Label")
			}
		when: "canvas 2 is loaded"
			loadSignalPath(name2)
		then: "Label can be found"
			waitFor {
				!findModuleOnCanvas("Table")
				findModuleOnCanvas("Label")
			}
		when: "clicked back button"
			driver.navigate().back()
		then: "Table can be found, no Label"
			waitFor {
				findModuleOnCanvas("Table")
				!findModuleOnCanvas("Label")
			}
	}

	def "going back after opening a canvas from list goes back to the list"() {
		setup:
			to CanvasListPage
		when: "canvas 1 is loaded"
			clickRow("CanvasSpec test loading a SignalPath")
		then: "Add can be found"
			waitFor {
				at CanvasPage
				findModuleOnCanvas("Add")
			}
		when: "clicked back button"
			driver.navigate().back()
		then:
			waitFor {
				at CanvasListPage
			}
	}

	def "going back after opening a canvas from list goes back to the list, even after reloading the canvas"() {
		setup:
			to CanvasListPage
		when: "canvas 1 is loaded"
			clickRow("CanvasSpec test loading a SignalPath")
		then: "Add can be found"
			waitFor {
				at CanvasPage
				findModuleOnCanvas("Add")
			}
		when: "reloaded"
			driver.navigate().refresh()
		then: "Add can be found"
			waitFor {
				at CanvasPage
				findModuleOnCanvas("Add")
			}
		when: "reloaded again"
			driver.navigate().refresh()
		then: "Add can be found once again"
			waitFor {
				at CanvasPage
				findModuleOnCanvas("Add")
			}
		when: "clicked back button"
			driver.navigate().back()
		then:
			waitFor {
				at CanvasListPage
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
			loadSignalPath 'CanvasSpec test loading a SignalPath'
		then: "signalpath content must be loaded"
			waitFor {
				findModuleOnCanvas("Add")
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
			loadSignalPath("CanvasSpec test loading a SignalPath")
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
			runHistoricalButton.click()
		then: "output should be produced"
			waitFor(30) {
				runHistoricalButton.text().contains("Abort")
				$('.modulebody .table td', text: "2015-02-23 18:30:00.011")
			}

		when: "abort button is clicked"
			runHistoricalButton.click()
			sleepForNSeconds(2) // Allow some time for server-side stuff to clean up
		then: "button must change back to run"
			waitFor {
				runHistoricalButton.text().contains("Run")
			}
	}

	def "running a SignalPath on current day should read from Kafka"() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
		Date now = new Date()
		String sNow = df.format(now)

		when: "SignalPath is loaded"
			loadSignalPath 'test-run-canvas'
		then: "signalpath content must be loaded"
			moduleShouldAppearOnCanvas('Table')

		when: "Run options button is clicked"
			historicalOptionsButton.click()
		then: "Speed option must be shown"
			waitFor { speed.displayed }

		when: "The Full speed option is selected and modal is dismissed"
			speed.click()
			speed.find("option").find{ it.value() == "0" }.click()
			historicalOptionsModal.find(".btn-primary").click()
		then: "Modal must disappear"
			waitFor { !historicalOptionsModal.displayed }

		when: "data is produced and signalpath is run on current date"
			UnifinaKafkaProducer kafka = makeKafkaProducer()
			// Procuce to the stream that test-run-canvas reads from
			kafka.sendJSON("c1_fiG6PTxmtnCYGU-mKuQ", "", now.getTime(), '{"temperature": '+Math.random()+'}')
			beginDate.click()
			beginDate = "2015-02-27"
			beginDate << Keys.TAB
			endDate.click()
			endDate = df.format(new Date())
			endDate << Keys.TAB
			runHistoricalButton.click()
		then: "output should be produced and there should be more rows than what exists for 2015-02-27"
			waitFor(30) {
				$('.modulebody .table tr').size() > 554
			}

	}

	void "module help shows and hides tooltip"() {
		setup:
			addAndWaitModule 'Add'
		when: "module help button is hovered"
			def helpButton = $(".moduleheader .modulebutton", 0)
			interact {
				moveToElement(helpButton)
			}
		then: "the tooltip should show up"
			waitFor {
				$(".tooltip .modulehelp-tooltip").displayed
				$(".tooltip .modulehelp-tooltip p", 1).text().contains("Adds together")
			}
		when: "mouse moved away"
			def menu = $(".menu-content")
			interact {
				moveToElement(menu)
			}
		then: "tooltip should be hidden"
			waitFor {
				!$(".tooltip .modulehelp-tooltip").displayed
			}
	}

	void "IOSwitches show tooltips"() {
		setup:
			addAndWaitModule 'Add'
		when: "input ioSwitch is hovered"
			def el = $("td.input .ioSwitch.drivingInput")
			interact {
				moveToElement(el)
			}
		then: "input ioSwitch tooltip is displayed"
			waitFor {
				$(".tooltip").displayed
				$(".tooltip .tooltip-inner").text().contains("Driving input")
			}
		when: "mouse moved away"
			def menu = $(".menu-content")
			interact {
				moveToElement(menu)
			}
		then: "input ioSwitch is no longer displayed"
			waitFor {
				!$(".tooltip .modulehelp-tooltip").displayed
			}

		when: "output ioSwitch is hovered"
			el = $("td.output .ioSwitch.noRepeat")
			interact {
				moveToElement(el)
			}
		then: "output ioSwitch tooltip is displayed"
			waitFor {
				$(".tooltip").displayed
				$(".tooltip .tooltip-inner").text().contains("No repeat")
			}
		when: "mouse moved away"
			menu = $(".menu-content")
			interact {
				moveToElement(menu)
			}
		then: "output ioSwitch tooltip is no longer displayed"
			waitFor {
				!$(".tooltip .modulehelp-tooltip").displayed
			}
	}

	void "IOSwitch tooltip shows the value of the switch"() {
		def ioSwitch

		when: "module is added"
			addAndWaitModule 'Add'
		then: "ioSwitches are visible by default"
			waitFor {
				$("td.input .ioSwitch.drivingInput").displayed
			}

		when: "input ioSwitch is hovered"
			ioSwitch = $("td.input .ioSwitch.drivingInput").first()
			interact {
				moveToElement(ioSwitch)
			}
		then: "input ioSwitch tooltip show value 'on'"
			waitFor {
				$(".tooltip").displayed
				$(".tooltip .tooltip-inner strong").text() == "on"
			}
		when: "tooltip is hidden, switch is clicked and tooltip is shown again"
			def menu = $(".menu-content")
			interact {
				moveToElement(menu)
			}
			ioSwitch.click()
			interact {
				moveToElement(ioSwitch)
			}
		then: "input ioSwitch tooltip show value 'on'"
			waitFor {
				$(".tooltip").displayed
				$(".tooltip .tooltip-inner strong").text() == "off"
			}
	}

}
