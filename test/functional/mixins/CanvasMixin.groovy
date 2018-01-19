package mixins

import com.unifina.domain.signalpath.Canvas
import geb.navigator.Navigator
import org.openqa.selenium.Keys

import java.util.concurrent.TimeUnit

trait CanvasMixin {
	
	void selectModuleInModuleBrowser(name, clickMethod = "click") {
		js.exec """
			(function() {
				var found = null
				\$("#moduleTree li.jstree-leaf a").each(function(i,el) {
					if (\$(el).text().trim()=="$name"){
						found = el
					}
				})
				if (found){
					\$(found).${clickMethod}()
				}
			})()
		"""
	}
	
	void selectCategoryInModuleBrowser(name) {
		js.exec """
			(function() {
				var found = null
				\$("#moduleTree li:not(.jstree-leaf) a").each(function(i,el) {
					if (\$(el).text().trim()=="$name")
						found = el
				})
				if (found)
					\$(found).click()
			})()
		"""
	}

	void addModule(name) {
		selectModuleInModuleBrowser(name)
		waitFor {
			moduleTree.find('a.jstree-clicked', text: contains(name))
		}
		// TODO: revert to previous version (clicking add module button) once CORE-1033 is fixed.
		def listItem = moduleTree.find('a.jstree-clicked', text: contains(name))
		interact { doubleClick(listItem) }
	}

	void addAndWaitModule(name) {
		addModule(name)
		moduleShouldAppearOnCanvas(name)
	}

	void dragAndDropModule(name, x=200, y=200) {
		selectModuleInModuleBrowser(name)
		def el = moduleTree.find('a.jstree-clicked', text: contains(name))

		interact {
			dragAndDropBy(el, x, y)
		}
	}

	void moveModuleBy(name, x, y, index=0, dragByHeader=false) {
		def module = findModuleOnCanvas(name, index)
		if (dragByHeader) {
			module = module.find(".modulename")
		}

		interact {
			clickAndHold(module)
			moveByOffset(x, y)
			release()
		}
	}

	void searchAndClick(name, input = search) {
		input << name
		waitFor {
			$('.streamr-search-suggestion .streamr-search-suggestion-name', text: ~/(?i)^${name}$/)
		}
		js.exec  """
				\$('.streamr-search-suggestion .streamr-search-suggestion-name').filter(function() {
					return \$(this).text().toLowerCase() == '${name.toLowerCase()}';
				}).click()
			"""
	}
	
	void searchAndClickContains(name, input = search) {
		searchForModule(name, input)
		clickSearchResult(name)
	}

	void searchForModule(name, input = search) {
		input << name
		waitFor {
			$('.streamr-search-suggestion', text:iContains(name))
		}
	}

	def clickSearchResult(name) {
		js.exec """
			\$('.streamr-search-suggestion').filter(function() {
				return \$(this).text().toLowerCase().indexOf('${name.toLowerCase()}') !== -1;
			}).click()
		"""
	}

	void moduleShouldAppearOnCanvas(name, index=0) {
		waitFor {
			findModuleOnCanvas(name, index).displayed
		}
	}

	def findModuleOnCanvas(name, index=0) {
		canvas.find('.moduleheader .modulename', text: name)[index].parents(".component")
	}

	def findModuleByHash(hash) {
		return $("#module_$hash")
	}

	def closeModule(String moduleLabel) {
		def module = findModuleOnCanvas(moduleLabel)
		module.find("i.delete").click()
	}

	void loadSignalPath(name) {
		js.exec """
			loaded = false;
			\$(SignalPath).on('loaded', function() { loaded = true; });
		"""
		$('#loadSignalPath').click()
		waitFor {
			$('#archiveLoadBrowser table td', text: name)
		}
		$('#archiveLoadBrowser table td', text: name).displayed
		$('#archiveLoadBrowser table td', text: name).click()
		waitFor {
			!$('#modal-spinner').displayed
		}
		waitFor { js.loaded }
	}
	
	def findInput(module, ioName, index=0) {
		findEndpoint(module, ioName, "input", index)
	}

	def findInputByDisplayName(module, name, index=0) {
		findEndpointByDisplayName(module, name, "input", index)
	}
	
	def findOutput(module, ioName, index=0) {
		findEndpoint(module, ioName, "output", index)
	}

	def findOutputByDisplayName(module, name, index=0) {
		findEndpointByDisplayName(module, name, "output", index)
	}
	
	def findEndpoint(module, ioName, type, index=0) {
		String moduleId
		if (module instanceof Navigator)
			moduleId = module.getAttribute("id")
		else moduleId = findModuleOnCanvas(module, index).getAttribute("id")
	
		String getter = (type=="input" ? "getInput" : "getOutput")
		String endpointId = js.exec("return \$('#${moduleId}').data('spObject').${getter}('${ioName}').div.attr('id')")
		return $("#"+endpointId)
	}

	def findEndpointByDisplayName(module, name, type, index=0) {
		String moduleId
		if (module instanceof Navigator)
			moduleId = module.getAttribute("id")
		else moduleId = findModuleOnCanvas(module, index).getAttribute("id")

		String getter = (type=="input" ? "findInputByDisplayName" : "findOutputByDisplayName")
		String endpointId = js.exec("return \$('#${moduleId}').data('spObject').${getter}('${name}').div.attr('id')")
		return $("#"+endpointId)
	}

	def toggleExport(String moduleLabel, String endpointLabel) {
		openContextMenu(moduleLabel, endpointLabel)
		clickFromContextMenu("Toggle export")
	}

	def renameEndpoint(String moduleLabel, String endpointLabel, String newName) {
		openContextMenu(moduleLabel, endpointLabel)
		clickFromContextMenu("Rename")
		waitFor { $(".rename-endpoint-dialog").displayed }
		$(".rename-endpoint-dialog input").firstElement().clear()
		$(".rename-endpoint-dialog input") << newName
		acceptConfirmation(".rename-endpoint-dialog")
	}

	def openContextMenu(String moduleLabel, String endpointLabel) {
		def module = findModuleOnCanvas(moduleLabel)
		def ioElem = module.find(".ioname", text: endpointLabel)
		if (ioElem.empty) {
			ioElem = module.find(".ioname", text: contains(endpointLabel))
		}
		ioElem[0].click()
		waitFor { $("#contextMenu").displayed }
	}

	def clickFromContextMenu(String label) {
		$("#contextMenu li a", text: label).click()
		waitFor { !$("#contextMenu").displayed }
	}

	def setParameterValueForModule(String moduleLabel, String parameterLabel, String value) {
		def module = findModuleOnCanvas(moduleLabel)
		def input = module.find(".ioname", text: parameterLabel).parents("tr").find("input")
		input.firstElement().clear()
		input << value
	}

	def chooseDropdownParameterForModule(String moduleLabel, String endpointLabel, String choiceText) {
		def module = findModuleOnCanvas(moduleLabel)
		def select = module.find(".ioname", text: endpointLabel).parents("tr").find("select")
		select.click()
		select.find("option").find{ it.text() == choiceText }.click()
	}
	
	def getJSPlumbEndpoint(endpoint) {
		def id = endpoint.getAttribute("id")
		def jsPlumbEndpointId = js.exec("return \$('#${id}').data('spObject').jsPlumbEndpoint.canvas.id")
		
		// Assign the endpoint an id if it doesn't have one, so that it can be queried via Geb
		if (!jsPlumbEndpointId) {
			jsPlumbEndpointId = "temp_"+new Date().time
			js.exec("\$('#${id}').data('spObject').jsPlumbEndpoint.canvas.id = '${jsPlumbEndpointId}'")
		}
		return $("#"+jsPlumbEndpointId)
	}
	
	void connectEndpoints(output, input) {
		def outputId = output.getAttribute("id")
		def inputId = input.getAttribute("id")
		js.exec "\$('#${inputId}').data('spObject').connect(\$('#${outputId}').data('spObject'))"
	}

	void disconnectEndpoint(endpoint) {
		def endpointId = endpoint.getAttribute("id")
		js.exec "\$('#${endpointId}').data('spObject').disconnect()"
	}
	
	void selectFromContextMenu(element, optionName) {
		interact{ contextClick(element) }
		waitFor { $("#contextMenu").displayed }
		$("#contextMenu a", text: optionName).click()
	}

	void saveCanvasAs(String name) {
		saveDropdownButton.click()
		saveAsButton.click()
		waitFor { $(".save-as-name-dialog").displayed }
		$(".save-as-name-dialog input").firstElement().clear()
		$(".save-as-name-dialog input") << name
		$(".save-as-name-dialog button.btn-primary").click()
		waitFor (20) { $(".ui-pnotify").displayed }
		waitFor {
			!$(".save-as-name-dialog").displayed
			!$(".modal-backdrop").displayed
		}
	}

	void ensureHistoricalTabDisplayed() {
		if (!runTabHistorical.hasClass("active")) {
			historicalTabLink.click()
		}
		waitFor { runTabHistorical.displayed }
	}

	void ensureRealtimeTabDisplayed() {
		if (!runTabRealtime.hasClass("active")) {
			realtimeTabLink.click()
		}
		waitFor { runTabRealtime.displayed }
	}

	void saveCanvas() {
		saveDropdownButton.click()
		saveButton.click()
		waitFor { $(".ui-pnotify").displayed }
	}

	void stopCanvasIfRunning() {
		if (runRealtimeButton.text().contains("Stop")) {
			stopCanvas()
		}
	}

	void stopCanvas() {
		openUpStopConfirmation()
		acceptStopConfirmationAndWaitForStopped()
	}

	void openUpStopConfirmation() {
		runRealtimeButton.click()
		waitForConfirmation(".stop-confirmation-dialog")
	}

	void acceptStopConfirmationAndWaitForStopped() {
		acceptConfirmation(".stop-confirmation-dialog")
		waitForNotificationContaining("stopped")
		waitFor(30) { runRealtimeButton.text().contains("Start") }
	}

	void resetAndStartCanvas(boolean saveIfNecessary = false) {
		$(".active #runDropdown").click()
		startCanvas(saveIfNecessary, resetAndRunRealtimeButton)
	}

	void startCanvas(boolean saveIfNecessary = false, button = runRealtimeButton) {
		button.click()
		sleep(1000)
		if ($(".save-on-start-confirmation-dialog").displayed) {
			if (saveIfNecessary) {
				acceptConfirmation(".save-on-start-confirmation-dialog")
			} else {
				throw new RuntimeException("Canvas needs to be saved before starting.")
			}
		}
		waitFor { runRealtimeButton.text().contains("Stop") }
		waitForNotificationContaining("started")
	}

	void setCanvasName(String name) {
		canvasName.click()
		waitFor { canvasNameInput.displayed }
		canvasNameInput << name
		canvasNameInput << Keys.ENTER
		waitFor { canvasName.displayed }
	}

	void waitForCanvasContent() {
		waitFor {
			canvas.children(".module").size() > 0
		}
	}

	void waitForNotificationContaining(String text) {
		waitFor(30) {
				$(".ui-pnotify-text").findAll {
					it.displayed && it.text().toLowerCase().contains(text)
				}
		}
	}

	void noNotificationsVisible() {
		waitFor {
			$(".ui-pnotify-text").empty
		}
	}

	String getCanvasId() {
		return js.exec("return SignalPath.getId()")
	}

	Canvas.State getCanvasState(String id) {
		// Async callback signaling done is passed as last argument by executeAsyncScript
		// https://groups.google.com/forum/#!topic/geb-user/Lpi_4lroTcQ
		browser.driver.manage().timeouts().setScriptTimeout(2, TimeUnit.SECONDS);
		String stateString = browser.driver.executeAsyncScript("""
			var seleniumDone = arguments[arguments.length - 1]
			jQuery.getJSON(Streamr.createLink({uri: "api/v1/canvases/$id"})).done(function(result) {
				seleniumDone(result.state)
			}).fail(function(jqXHR, textStatus, errorThrown) {
				alert(errorThrown);
			})
		""")
		return Canvas.State.fromValue(stateString)
	}

	void turnOnSerialization() {
		realtimeOptionsButton.click()
		waitFor { realtimeOptionsModal.displayed }
		serializationEnabled.click()
		serializationEnabled.find("option").find { it.value() == "true" }.click()
		acceptConfirmation()
	}
}
