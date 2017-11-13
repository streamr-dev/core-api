package core.pages

import geb.Page

class CanvasPage extends Page {

	static at = {
		// wait until jsTree is loaded
		waitFor(20) {
			$('#moduleTree li.jstree-last li.jstree-last').size() > 0 &&
				$("#moduleTree > ul > li.jstree-last").size() > 0
		}
	}

	static controller = "canvas"
	static action = "editor"
	
	static url = "$controller/$action"

	static content = {
		navbar { module NavbarModule }
		newButton { $("#newSignalPath") }
		saveDropdownButton { $('#save-dropdown-button') }
		saveButton { $('#saveButton') }
		saveAsButton { $('#saveAsButton') }
		loadButton { $('#loadSignalPath') }
		shareButton { $('#share-button') }

		historicalTabLink { $("a[href='#tab-historical']") }
		runTabHistorical { $("#tab-historical") }
		runHistoricalButton { $('#run-historical-button') }
		runCsvExportModeButton { $("#csvModalButton") }
		runHistoricalDropdownButton { $('#tab-historical #runDropdown') }
		historicalOptionsButton { $('#historical-options-button') }
		historicalOptionsModal { $('#historicalOptionsModal') }

		realtimeTabLink { $("a[href='#tab-realtime']") }
		runTabRealtime { $("#tab-realtime") }
		runRealtimeButton { $('#run-realtime-button') }
		runRealtimeDropdownButton { $('#tab-realtime #runDropdown') }
		resetAndRunRealtimeButton { $("#run-realtime-clear") }
		realtimeOptionsButton { $('#realtime-options-button') }
		realtimeOptionsModal { $('#realtimeOptionsModal') }

		speed { $("#speed") }
		moduleTree { $('#moduleTree') }
		search { $('#search') }
		searchControl { $('#search-control') }
		canvas { $('#canvas') }
		addModuleButton { $('button#addModule') }
		modalSpinner { $('#modal-spinner') }
		beginDate { $("#beginDate") }
		endDate { $("#endDate") }

		canvasName { $("#canvas-name-editor") }
		canvasNameInput { $("#canvas-name-editor input") }
		canvasNameOkButton { $("#canvas-name-editor button") }
	}
}

