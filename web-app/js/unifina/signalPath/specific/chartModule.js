
SignalPath.ChartModule = function(data, canvas, prot) {
	prot = prot || {};
	var pub = SignalPath.UIChannelModule(data,canvas,prot)

	prot.enableIONameChange = false;	
		
	// Dragging in the chart container or the controls must not move the module
	prot.dragOptions.cancel = ".highcharts-container, .chart-series-buttons, .chart-range-selector"
    
    prot.range = data.range || "all"
	
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
		prot.chart = new StreamrChart(prot.body, $.extend(prot.jsonData.options, {
            range: prot.range
        }))
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
    
    var superToJSON = pub.toJSON;
    function toJSON() {
        prot.jsonData = superToJSON();
        prot.jsonData.range = prot.chart.range.value !== "all" ? prot.chart.range.value : undefined
        return prot.jsonData;
    }
    pub.toJSON = toJSON;
	
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
