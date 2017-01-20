SignalPath.ChartModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.UIChannelModule(data,canvas,prot)

	prot.enableIONameChange = false;	
		
	// Dragging in the chart container or the controls must not move the module
	prot.dragOptions.cancel = ".highcharts-container, .chart-series-buttons, .chart-range-selector"
	
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();
		
		// Change context menu on Double inputs
		if (!prot.jsonData.disableAxisSelection) {
			prot.div.find("div.input.Double").removeClass("default-context-menu").addClass("chart-context-menu");
		}

		initChart()
		
		prot.initResizable({
			minWidth: parseInt(prot.div.css("min-width").replace("px","")),
			minHeight: parseInt(prot.div.css("min-height").replace("px","")),
			stop: function(event,ui) {
				if (prot.chart)
					prot.chart.resize(ui.size.width, ui.size.height);
			}
		});
	}
	prot.createDiv = createDiv;	
	
	var super_createModuleFooter = prot.createModuleFooter
	prot.createModuleFooter = function() {
		var $footer = super_createModuleFooter()
		var $container = $footer.find(".moduleSwitchContainer")
		
		if (prot.jsonData.barify!==false)
			prot.jsonData.barify = true
		
		var barify = new SignalPath.IOSwitch($container, "moduleSwitch", {
			getValue: (function(d){
				return function() { return d.barify; };
			})(data),
			setValue: (function(d){
				return function(value) { return d.barify = value; };
			})(data),
			buttonText: function() { return "ECO"; },
			tooltip: 'Max one point per minute per series'
		})
		
		return $footer
	}
	
	// Create ChartInputs instead of ordinary Inputs
	var super_addInput = prot.addInput
	function addInput(data) {
		var input = super_addInput(data, SignalPath.ChartInput)
		input.div.on('yAxisChanged', function(event, name, yx) {
			if (prot.chart) {
				var seriesIndex = pub.getInput(name).seriesIndex
				if (seriesIndex !== null) {
					prot.chart.setYAxis(seriesIndex, yx)
				}
			}
		})
	}
	prot.addInput = addInput
	
	function getChart() {
		return prot.chart;
	}
	prot.getChart = getChart;

	function initChart() {
		prot.body.find(".ioTable").css("width","0px");
		prot.chart = new StreamrChart(prot.body, prot.jsonData.options)
		prot.chart.resize(prot.div.outerWidth(), prot.div.outerHeight())
	}
	
	function destroyChart() {
		if (prot.chart) {
			prot.chart.destroy()
		}
	}
	
	prot.receiveResponse = function(d) {
		prot.chart.handleMessage(d)
	}

	var startFunction = function(e, canvas) {
		// Reset all series indices
		pub.getInputs().forEach(function(input) {
			input.seriesIndex = null
		})
		var connectedInputs = pub.getInputs().filter(function(input) {
			return input.isConnected()
		})
		for (var i=0; i<connectedInputs.length; i++) {
			connectedInputs[i].seriesIndex = i
		}
		if (!canvas || !canvas.adhoc) {
			sendInitRequest()
		}
	}

	var stopFunction = function() {
		if (prot.chart && prot.chart.getSeriesMetaData().length > 1) {
			var seriesMeta = prot.chart.getSeriesMetaData()
			// Find connected inputs
			var connectedInputs = pub.getInputs().filter(function(input) {
				return input.isConnected()
			})

			// If series range is less than 10% of axis range, show a tip
			var popover = true
			for (var i=0; i<seriesMeta.length; i++) {
				if (seriesMeta[i].impl) {
					var yAxisRange = seriesMeta[i].impl.yAxis.getExtremes().max - seriesMeta[i].impl.yAxis.getExtremes().min
					var seriesRange = seriesMeta[i].max - seriesMeta[i].min
					
					if (seriesRange/yAxisRange < 0.1) {
						var $input = connectedInputs[i] 
						$input.div.data("spObject").showYAxisWarning(seriesMeta[i].impl.name, popover)
						
						// Show only one popover
						if (popover)
							popover = false
					}
				}
			}
		}
	}

	function sendInitRequest() {
		if (SignalPath.isRunning()) {
			SignalPath.runtimeRequest(pub.getRuntimeRequestURL(), {type: 'initRequest'}, function (response, err) {
				if (err)
					console.error("Failed initRequest for ChartModule: %o", err)
				else
					prot.chart.handleMessage(response.initRequest)
			})
		}
	}

	var superClose = pub.close;
	pub.close = function() {
		$(SignalPath).off("started", startFunction)
		$(SignalPath).off("stopped", stopFunction)
		$(SignalPath).off("loaded", sendInitRequest)
		superClose()
	}
	
	var superClean = pub.clean;
	pub.clean = function() {
		superClean()
		destroyChart()
	}
	
	var superUpdateFrom = pub.updateFrom;
	pub.updateFrom = function(data) {
		destroyChart();
		superUpdateFrom(data);
	}
	
	/**
	 * On start, bind connected inputs to series indices. We need
	 * to know which input results in which series.
	 */
	$(SignalPath).on("started", startFunction)
	
	/**
	 * On SignalPath stopped, check that all series are shown properly in relation to chart yaxis range
	 */
	$(SignalPath).on("stopped", stopFunction)

	$(SignalPath).on("loaded", sendInitRequest)
	
	return pub;
}

SignalPath.HighStocksModule = SignalPath.ChartModule;

/**
 * ChartInput is an Input with the following modifications:
 * - Input name is not shown
 * - Rename option is not shown in context menu
 * - Y-axis indicator button is shown for connected inputs
 * - Y-axis assignment can be cycled by clicking on the Y-axis indicator button 
 * 
 * Events:
 * - yAxisChanged(inputName, yAxis)
 */
SignalPath.ChartInput = function(json, parentDiv, module, type, pub) {
	pub = pub || {};
	pub = SignalPath.Input(json, parentDiv, module, type, pub);
	
	var btnDefaultClass = "btn-default"
	var btnPopoverClass = "btn-warning"
	var popoverClass = "popover-warning"
	
	var tooltipAxisId = generateId()

	// Use 1-based index for display to the user
	var displayedAxis = json.yAxis + 1
	// Cycle Y-axis button
	var $yAxisSelectorButton = $("<div class='y-axis-number btn "+btnDefaultClass+" btn-xs "+popoverClass+" popover-colorful'></div>")
	
	pub.seriesIndex = null
	pub.disableContextMenu = true
	
	var super_createDiv = pub.createDiv
	pub.createDiv = function() {
		var div = super_createDiv()
		div.bind("spConnect", function(event, output) {
			div.find(".y-axis-number").show()
			jsPlumb.repaint($(module.div).find("div.input"))
		})
		div.bind("spDisconnect", function(event, output) {
			div.find(".y-axis-number").hide()
			jsPlumb.repaint($(module.div).find("div.input"));
		})
		
		$yAxisSelectorButton.click(cycleYAxis)
		pub.div.tooltip({
			container: SignalPath.getParentElement(),
			selector: ".y-axis-number",
			html: true,
			title: function() {
				return "This input is drawn on y-axis <strong><span id='"+tooltipAxisId+"'>"+displayedAxis+"</span></strong>."
			}
		})
		updateButton()
		
		return div
	}
	
	function generateId() {
		var result = "tooltip_content_"+new Date().getTime()
		while ($("#"+result).size()>0)
			result = "tooltip_content_"+new Date().getTime()
		return result
	}
	
	function getNextYAxis(current) {
		// Find unique yaxis numbers and count how many inputs we have at each
		var inputs = module.getInputs()
		var connectedInputs = inputs.filter(function(input) {
			return input.isConnected()
		})
		var yAxisCounts = {}
		var yAxisUniqueIds = []
		connectedInputs.forEach(function(input) {
			if (yAxisCounts[input.json.yAxis.toString()]===undefined) {
				yAxisCounts[input.json.yAxis.toString()] = 1
				yAxisUniqueIds.push(input.json.yAxis)
			}
			else yAxisCounts[input.json.yAxis.toString()] = yAxisCounts[input.json.yAxis.toString()] + 1
		})
		
		// Sort yaxis ids numerically in ascending order
		yAxisUniqueIds.sort(function(a, b){return a-b})
		
		// Find the smallest free axis index
		var smallestFreeAxis = 0
		while (yAxisUniqueIds.indexOf(smallestFreeAxis)>=0)
			smallestFreeAxis++
		
		// If this input is the only one with in the current number, we're at the "free" position
		var atFree = (yAxisCounts[json.yAxis.toString()] === 1)
		
		// If we are at the "free" number, get the next valid number
		if (atFree) {
			return yAxisUniqueIds[(yAxisUniqueIds.indexOf(json.yAxis) + 1) % yAxisUniqueIds.length]
		}
		// Else if the next number if the smallest free number, return that
		else if (json.yAxis+1 === smallestFreeAxis) {
			return json.yAxis+1
		}
		// Else if we're at the end of the array, wrap to the lesser of (smallestFreeAxis, yAxisUniqueIds[0])
		else if (yAxisUniqueIds.indexOf(json.yAxis) === yAxisUniqueIds.length-1) {
			return Math.min(smallestFreeAxis, yAxisUniqueIds[0])
		}
		// Else just return the next number, regardless of free or not
		else {
			return json.yAxis + 1
		}
	}
	
	function updateButton() {
		displayedAxis = json.yAxis + 1
		$yAxisSelectorButton.html(displayedAxis)
		$("#"+tooltipAxisId).html(displayedAxis)
	}
	
	function cycleYAxis() {
		var oldYAxis = json.yAxis
		json.yAxis = getNextYAxis()
		updateButton()
		if (oldYAxis !== json.yAxis) {
			$(pub.div).trigger('yAxisChanged', [pub.getName(), json.yAxis])
		}
	}
	
	pub.getDisplayName = function(connected) {
		return $yAxisSelectorButton
	}
	
	var super_getContextMenu = pub.getContextMenu
	pub.getContextMenu = function(div) {
		var menu = []
		
		// Add y-axis cycle option (does the same thing as left-clicking the button)
		menu.push({title: "Cycle Y-axis", cmd: "yaxis"});

		// Chart inputs need not be renamed
		$(super_getContextMenu(div)).each(function(i,o) {
			if (o.title!=="Rename")
				menu.push(o)
		})

		return menu;
	}
	
	var super_handleContextMenuSelection = pub.handleContextMenuSelection
	pub.handleContextMenuSelection = function(target, selection) {
		if (selection=="yaxis")
			cycleYAxis()
		else super_handleContextMenuSelection(target, selection);
	}
	
	pub.showYAxisWarning = function(seriesName, popover) {
		
		if (popover) {
			$yAxisSelectorButton.popover({
				content: "Some series may not be showing properly. Use these buttons to cycle Y-axis assignments.",
				placement: "right",
				trigger: "manual"
			})
			$yAxisSelectorButton.popover('show')
		}
		
		$yAxisSelectorButton.removeClass(btnDefaultClass).addClass(btnPopoverClass)
		
		var destroyFunc = (function(b) {
			return function() { 
				b.popover('destroy')
				b.removeClass(btnPopoverClass).addClass(btnDefaultClass)
			}
		})($yAxisSelectorButton)
		
		$yAxisSelectorButton.siblings(".popover").click(destroyFunc)
		setTimeout(destroyFunc, 8000)
	}
	
	return pub;
}