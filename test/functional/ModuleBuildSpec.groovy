import core.LoginTester1Spec
import core.mixins.CanvasMixin
import org.openqa.selenium.Keys

class ModuleBuildSpec extends LoginTester1Spec {

	def setupSpec() {
		// @Mixin is buggy, use runtime mixins instead
		ModuleBuildSpec.metaClass.mixin(CanvasMixin)
	}
	
	def "cloning a module should produce a duplicate"() {
		when: "Barify is added via module browser"
			addModule 'Barify'
		then: "module should appear on canvas"
			moduleShouldAppearOnCanvas 'Barify'

		when: "clone option is selected from module context menu"
			selectFromContextMenu(findModuleOnCanvas("Barify"), "Clone module")
		then: "there should be another module on canvas"
			waitFor { canvas.find(".component").size() == 2 }
	}
	
	def "module help button functionality"() {
		when: "Barify is added via module browser"
			addModule 'Barify'
		then: "module should appear on canvas"
			moduleShouldAppearOnCanvas 'Barify'
			
		when: "hovering on help button"
			findModuleOnCanvas("Barify").find(".modulebutton .help").jquery.mouseover()
		then: "tooltip must be displayed"
			waitFor { canvas.find(".tooltip") }
			
		when: "moving away from help button"
			findModuleOnCanvas("Barify").find(".modulebutton .help").jquery.mouseleave()
		then: "tooltip must be closed"
			waitFor { !canvas.find(".tooltip") } 

		when: "help button is clicked"
			findModuleOnCanvas("Barify").find(".modulebutton .help").click()
			// Explicitly trigger mouseleave on the button. Without this the tooltip
			// is not closed and $(".modal-dialog").displayed will be false for
			// some reason. This issue could not be replicated manually.
			findModuleOnCanvas("Barify").find(".modulebutton .help").jquery.mouseleave()
		then: "help modal is opened"
			waitFor { $(".modal-dialog .modulehelp").displayed }

		when: "modal is dismissed"
			$(".modal-dialog .modulehelp").parents(".modal-dialog").find(".modal-header button.close").click()
		then: "modal must close"
			waitFor { !$(".modal-dialog .modulehelp") }
	}	
	
	def "module options button functionality"() {
		when: "the Map module is added via module browser"
			addModule 'Map'
		then: "module should appear on canvas"
			moduleShouldAppearOnCanvas 'Map'
		then: "module zoom level != 12"
			mapZoomLevel() != 12
			
		when: "options button is clicked"
			findModuleOnCanvas("Map").find(".modulebutton .options").click()
		then: "options modal is shown"
			waitFor { $(".modal-dialog .optionEditor").displayed }
			
		// Test for issue CORE-130: cancel and reopen options dialog
		when: "options modal is canceled"
			$(".modal-dialog .optionEditor").parents(".modal-dialog").find(".btn.btn-default").click()
		then: "dialog is closed"
			waitFor { !$(".modal-dialog .optionEditor") }
		
		when: "options button is clicked"
			findModuleOnCanvas("Map").find(".modulebutton .options").click()
		then: "options modal is shown"
			waitFor { $(".modal-dialog .optionEditor").displayed }
			
		when: "zoom level is changed to 12 and options are OK'ed"
			def zoomElementIndex = $(".modal-dialog .optionEditor input").findIndexOf { it.text() == "zoom" }
			$(".modal-dialog .optionEditor input", zoomElementIndex).value("12")
			$(".modal-dialog .optionEditor").parents(".modal-dialog").find(".btn.btn-primary").click()
		then: "dialog is OK'ed"
			waitFor { !$(".modal-dialog .optionEditor") }
		then: "module must be reloaded with zoom level 12"
			mapZoomLevel() == 12
	}


	def mapZoomLevel() {
		js.exec('return $("#module_0").data("spObject").getMap().map.getZoom();')
	}
	
	def "module context menu"() {
		when: "the Add module is added via module browser"
			addModule 'Add'
		then: "module should appear on canvas"
			moduleShouldAppearOnCanvas 'Add'
			
		when: "the module body is context-clicked"
			interact{ contextClick(findModuleOnCanvas("Add").find(".moduleheader")) }
		then: "a context menu must appear"
			waitFor { $("#contextMenu").displayed }
	}
	
	def "endpoint context menu"() {
		when: "the Add module is added via module browser"
			addModule 'Add'
		then: "module should appear on canvas"
			moduleShouldAppearOnCanvas 'Add'
			
		when: "an output div is context-clicked"
			interact{ contextClick(findModuleOnCanvas("Add").find(".endpoint.output")) }
		then: "a context menu must appear related to the endpoint with toggle export selection"
			waitFor { $("#contextMenu a", text:"Toggle export").displayed }
			
		when: "a selection is made"
			$("#contextMenu a", text:"Toggle export").click()
		then: "the context menu is hidden"
			waitFor { !$("#contextMenu a", text:"Toggle export").displayed }
	}

	def "unconnected input warnings"() {
		when: "a module is added"
			addModule "Add"
		then: "module must appear"
			moduleShouldAppearOnCanvas "Add"
		then: "input must contain the warning class"
			findInput("Add", "in1").classes().contains("warning")
			
		when: "another module is added"
			searchAndClick "CanvasSpec"
		then: "it must appear"
			moduleShouldAppearOnCanvas "Stream"
			
		when: "the input is connected to an output"
			connectEndpoints(findOutput("Stream","temperature"), findInput("Add","in1"))
		then: "the input must no longer have the warning class"
			!findInput("Add", "in1").classes().contains("warning")
			
		when: "the module is disconnected"
			selectFromContextMenu(findModuleOnCanvas("Stream"), "Disconnect all")
		then: "the warning class must reappear"
			findInput("Add", "in1").classes().contains("warning")
	}
	
	def "moving an input connection"() {
		when: "a module is added"
			addModule "Add"
		then: "module must appear"
			moduleShouldAppearOnCanvas "Add"
		then: "both inputs must contain the warning class"
			findInput("Add", "in1").classes().contains("warning")
			findInput("Add", "in2").classes().contains("warning")
		
		when: "a connection is made to in1"
			connectEndpoints(findOutput("Add","sum"), findInput("Add","in1"))
		then: "the input must no longer have the warning class"
			!findInput("Add", "in1").classes().contains("warning")
			findInput("Add", "in2").classes().contains("warning")
			
		when: "the connection is dragged from in1 to in2"
			interact {
				clickAndHold(getJSPlumbEndpoint(findInput("Add", "in1")))
				moveToElement(getJSPlumbEndpoint(findInput("Add", "in2")))
				release()
			}
		then: "in1 must have warning and in2 not"
			findInput("Add", "in1").classes().contains("warning")
			!findInput("Add", "in2").classes().contains("warning")
	}
	
	def "setting an initial value removes input warning"() {
		when: "a module is added"
			addModule "Add"
		then: "module must appear"
			moduleShouldAppearOnCanvas "Add"
		then: "input must contain the warning class"
			findInput("Add", "in1").classes().contains("warning")
			
		when: "initial value switch is clicked"
			findInput("Add", "in1").find(".ioSwitch.initialValue").click() // set to 0
		then: "initial value prompt is displayed"
			waitFor {
				$(".initial-value-dialog input").displayed
				$(".initial-value-dialog .btn-primary").displayed
			}
			
		when: "an initial value is entered and modal is dismissed"
			$(".initial-value-dialog input") << "0"
			$(".initial-value-dialog .btn-primary").click()
		then: "dialog is closed"
			waitFor { $(".initial-value-dialog").size() == 0 }
		then: "warning class is no longer shown"
			!findInput("Add", "in1").classes().contains("warning")
		
		when: "the initial value is removed"
			findInput("Add", "in1").find(".ioSwitch.initialValue").click() // toggle to null
		then: "warning class must reappear"
			findInput("Add", "in1").classes().contains("warning")
	}
	
	def "inputs must be highlighted when dragging a connection from output, and vice versa"() {
		when: "a module is added"
			addModule "Add"
		then: "module must appear"
			moduleShouldAppearOnCanvas "Add"
		then: "no endpoints are highlighted"
			$("._jsPlumb_endpoint.highlight").size()==0
		then: "no connectors are shown"
			$("._jsPlumb_connector").size()==0
			
		when: "dragging from output"
			def output = getJSPlumbEndpoint(findOutput("Add","sum"))
			interact {
				clickAndHold(output)
				moveByOffset(100, 100)
			}
		then: "a connector must appear"
			$("._jsPlumb_connector").size()>0
		then: "inputs must highlight"
			$(".jsPlumb_input.highlight").size()==3
			
		when: "connection is dropped"
			interact {
				release()
			}
		then: "no endpoints are highlighted"
			$("._jsPlumb_endpoint.highlight").size()==0
			
		when: "dragging from input"
			def input = getJSPlumbEndpoint(findInput("Add","in1"))
			interact {
				clickAndHold(input)
				moveByOffset(100, 100)
			}
		then: "the output must highlight"
			$(".jsPlumb_output.highlight").size()==1
			
		when: "connection is dropped"
			interact {
				release()
			}
		then: "no endpoints are highlighted"
			$("._jsPlumb_endpoint.highlight").size()==0
	}
	
	def "endpoints with non-matching type must not highlight"() {
		setup:
			addModule "Add"
			moduleShouldAppearOnCanvas "Add"
			def addHandle = findModuleOnCanvas("Add").find(".modulename")
			def addInput = getJSPlumbEndpoint(findInput("Add","in1"))
			interact {
				dragAndDropBy(addHandle, 200, 0)
			}
			
			addModule "Contains"
			moduleShouldAppearOnCanvas "Contains"
			def containsHandle = findModuleOnCanvas("Contains").find(".modulename")
			def containsInput = getJSPlumbEndpoint(findInput("Contains","search"))
			interact {
				dragAndDropBy(containsHandle, 200, 200)
			}
			
			searchAndClick "CanvasSpec"
			moduleShouldAppearOnCanvas "Stream"
			def temperatureOutput = getJSPlumbEndpoint(findOutput("Stream", "temperature"))
			def textOutput = getJSPlumbEndpoint(findOutput("Stream", "text"))
			
		when: "a Double connection is being made"
			interact {
				clickAndHold(temperatureOutput)
				moveByOffset(100, 100)
			}
		then: "Double connection must highlight"
			addInput.classes().contains("highlight")
		then: "Trades input must not highlight"
			!textOutput.classes().contains("highlight")
			
		when: "a Trades connection is being made"
			interact {
				release()
			}
			interact {
				clickAndHold(textOutput)
				moveByOffset(100, 100)
			}
		then: "Trades input must highlight"
			containsInput.classes().contains("highlight")
		then: "Double connection must not highlight"
			!addInput.classes().contains("highlight")
	}
	
	def "stream parameter change"() {
		when: "CanvasSpec is added"
			searchAndClick("CanvasSpec")
		then: "An Orderbook must appear"
			moduleShouldAppearOnCanvas "Stream"
			
		when: "the CanvasSpec parameter link is clicked"
			def ob = findModuleOnCanvas "Stream"
			ob.find(".streamName").click()
		then: "an input must be shown"
			ob.find(".streamSearch").displayed
			
		when: "search term is changed to 'xyzzy'"
			ob.find(".streamSearch.streamr-search-input").firstElement().clear()
			ob.find(".streamSearch.streamr-search-input") << "xyzzy"
		then: "ModuleBuildSpec stream must be shown in suggestions"
			waitFor {
				$('.streamr-search-suggestion-name', text: contains("ModuleBuildSpec"))
			}
			
		when: "suggestion is clicked"
			$('.streamr-search-suggestion-name', text: contains("ModuleBuildSpec")).click()
		then: "stream is changed"
			waitFor {
				$(".streamName", text: "ModuleBuildSpec")
			}
	}
	
}
