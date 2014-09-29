SignalPath.ChartModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	prot.enableIONameChange = false;
	
	var area = null;
	
	var chart = null;
	var chartTitle = null;
	var yAxis = null;
	var minTime = null;
	var maxTime = null;
	
	var realYAxis = [];
	
	var seriesMeta = [];
	
	var range = null;
	
	// Dragging in the chart container or the controls must not move the module
	prot.dragOptions.cancel = ".highcharts-container, .chart-series-buttons, .chart-range-selector"

	function resizeChart(moduleWidth, moduleHeight) {
		if (!area)
			return;

		var w = moduleWidth - 100
		var h = moduleHeight - 110

		area.css('width', w)
		area.css('height', h)

		if (chart)
			chart.setSize(w, h, false)
	}
	
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();
		
		// Change context menu on Double inputs
		if (!prot.jsonData.disableAxisSelection) {
			prot.div.find("div.input.Double").removeClass("default-context-menu").addClass("chart-context-menu");
		}

		initArea();
		
		prot.initResizable({
			minWidth: parseInt(prot.div.css("min-width").replace("px","")),
			minHeight: parseInt(prot.div.css("min-height").replace("px","")),
			stop: function(event,ui) {
				resizeChart(ui.size.width, ui.size.height);
			}
		});
	}
	prot.createDiv = createDiv;	
	
	// Create ChartInputs instead of ordinary Inputs
	var super_addInput = prot.addInput
	function addInput(data) {
		var input = super_addInput(data, SignalPath.ChartInput)
		input.div.on('yAxisChanged', function(event, name, yx) {
			if (chart) {
				var seriesIndex = pub.getInput(name).seriesIndex
				if (seriesIndex !== null) {
					
					// Add a new yAxis to the chart if necessary
					if (realYAxis[yx]===undefined) {
						var newAxis = createYAxisOptions({}, chart.yAxis.length)
						realYAxis[yx] = chart.yAxis.length
						yAxis.push(newAxis)
						chart.addAxis(newAxis, false)
					}
					
					chart.series[seriesIndex].update({yAxis: realYAxis[yx]}, true)
				}
			}
		})
	}
	prot.addInput = addInput
	
	function getChart() {
		return chart;
	}
	prot.getChart = getChart;

	function showHide(doShow) {
		return function() {
			if (!chart)
				return;

			chart.series.forEach(function(series) {
				series.setVisible(doShow, false)
			})
			chart.redraw()
		}
	}

	function initArea() {
		prot.body.find(".ioTable").css("width","0px");
		
		// Find the chart draw area
		area = prot.body.find(".chartDrawArea");
		if (area==null || area.length==0) {

			// Show/Hide all series buttons
			prot.body.append('<div class="chart-series-buttons pull-right btn-group">' +
				'<button class="btn btn-default btn-sm show-all-series" '+
					'title="Show all series"><i class="fa fa-plus-circle"></i></button>'+
				'<button class="btn btn-default btn-sm hide-all-series" '+
					'title="Hide all series"><i class="fa fa-minus-circle"></i></button>'+
			'</div>')

			$('button.hide-all-series', prot.body).click(showHide(false))
			$('button.show-all-series', prot.body).click(showHide(true))
			
			// Range selector
			var $rangeDiv = $("<select class='chart-range-selector form-control pull-right' title='Range'></select>");
			var buttonConfig = [{name:"1 sec",range:1*1000},
			                    {name:"15 sec",range:15*1000},
			                    {name:"1 min",range:60*1000},
			                    {name:"15 min",range:15*60*1000},
			                    {name:"30 min",range:30*60*1000},
			                    {name:"1 h",range:60*60*1000},
			                    {name:"2 h",range:2*60*60*1000},
			                    {name:"4 h",range:4*60*60*1000},
			                    {name:"12 h",range:12*60*60*1000},
			                    {name:"day",range:24*60*60*1000},
			                    {name:"week",range:7*24*60*60*1000},
			                    {name:"month",range:30*24*60*60*1000},
			                    {name:"All",range:""}]
			
			buttonConfig.reverse()
			buttonConfig.forEach(function(c) {
				var  $option =  $("<option value='"+c.range+"'>"+c.name+"</option>")
				$rangeDiv.append($option)
			})
			
			$rangeDiv.on('change', function() {
				var r = $(this).val()
				if (r) {
					r = parseInt(r)
				}
				else r = null
				
				range = r
				if (chart)
					redrawChart()
			})

			prot.body.append($rangeDiv);
			
			// Create the chart area
			var areaId = "chartArea_"+(new Date()).getTime()
			area = $("<div id='"+areaId+"' class='chartDrawArea'></div>")
			prot.body.append(area)
			
			resizeChart(prot.div.width(), prot.div.height())
		}
	}
	
	function createRangeButtons(buttonDiv,config) {
		config.forEach(function(c) {
			var button = $('<button class="btn btn-default btn-sm">'+c.name+"</button>")
			button.click((function(r) {
				return function() {
					range = r
					redrawChart()
				}
			})(c.range))
			buttonDiv.append(button)
		})
	}
	
	function destroyChart() {
		if (chart!=null) {
			chart.destroy();
			prot.body.find("button.yAxis").remove();
			chart=null;
		}
	}
	
	function createYAxisOptions(options, n) {
		var result = $.extend({
			opposite: (n%2==0),
			offset: (Math.floor(n/2)+1)*30,
			title: {
				text: ""
			}
		}, SignalPath.defaultChartOptions.yAxis || {}, options)
		return result
	}
	
	function initChart(title,series,yAxis) {
		destroyChart();

		$(area).show();
		
		if (yAxis==null) {
			yAxis = {};
		}
		else if ($.isArray(yAxis)) {
			for (var i=0;i<yAxis.length;i++)
				yAxis[i] = createYAxisOptions(yAxis[i], i);
		}

		Highcharts.setOptions({
			global: {
				useUTC: true
			}
		});

		var opts = {
				chart: {
					animation: false,
					renderTo: area.attr("id"),
					panning: true,
					spacingBottom: 40,
					backgroundColor: null,
					zoomType: 'x'
				},

				credits: {
					enabled: false
				},

				xAxis: {
					ordinal: false
				},

				yAxis: yAxis, 

				legend: {
					enabled: true,
					// maxHeight: 100,
					// y: 40
				},
				
				rangeSelector: {
					enabled: false
				},

				navigator: {
					enabled: false
				},
				
				scrollbar: {
					enabled: true
				},
				
				series: series
		};

		opts = $.extend(true, {}, SignalPath.defaultChartOptions || {}, opts);
		
		// Create the chart	
		chart = new Highcharts.StockChart(opts);
	}
	
	pub.receiveResponse = function(d) {
		if (area==null)
			initArea();
		
		// Data point message
		if (d.type=="p") {
			if (minTime==null || d.x!=null && d.x<minTime)
				minTime = d.x;
			if (maxTime==null || d.x!=null && d.x>maxTime)
				maxTime = d.x;
			
			if (chart==null) {
				if (seriesMeta[d.s].data==null) {
					// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
					seriesMeta[d.s].data = [[d.x, d.y]];
				}
				// Init the chart when any of the series gets it's 2nd data point
				else if (seriesMeta[d.s].data.length>=1) {
					// Add one more point to prevent coming here again
					// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
					seriesMeta[d.s].data.push([d.x, d.y]);


					// Init all other series to have at least two points
					for (var i=0;i<seriesMeta.length;i++) {
						if (i!=d.s) {
							// Add null points if no data received yet
							if (seriesMeta[i].data==null) {
								seriesMeta[i].data = [];
								// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
								seriesMeta[i].data.push([seriesMeta[d.s].data[0][0], null]);
								seriesMeta[i].data.push([d.x,null]);
							}
							// If one point received, repeat first value
							else if (seriesMeta[i].data.length==1) {
								// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
								seriesMeta[i].data.push([d.x, seriesMeta[i].data[0][1]]);
							}
						}
					}
					
					initChart(chartTitle,seriesMeta,yAxis);
				}
			}
			// Chart has already been initialized
			else {
				// addPoint is slow?
				if (d.s<chart.series.length) {
					// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
					chart.series[d.s].addPoint([d.x, d.y],false,false,false);
					if (seriesMeta[d.s].min > d.y)
						seriesMeta[d.s].min = d.y
					if (seriesMeta[d.s].max < d.y)
						seriesMeta[d.s].max = d.y
				}
				// Are there pending series adds in seriesMeta?
				else {
					if (seriesMeta[d.s].data==null)
						seriesMeta[d.s].data = [];
					
					// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
					seriesMeta[d.s].data.push([d.x, d.y]);
					
					for (var i=chart.series.length;i<seriesMeta.length;i++) {
						// Unfortunately we need to add series in order
						if (seriesMeta[i].data!=null && seriesMeta[i].data.length>1)
							chart.addSeries(seriesMeta[i],true,false);
						else break;
					}
				}
			}
		}
		
		// Init message
		else if (d.type=="init") {
			
			yAxis = d.yAxis || [];
			
			var x=0;
			var seenYAxisNumbers = []
			// Remap the "user" yAxis assignments to actual Highcharts ones,
			// which need to be ascending and without gaps
			$(d.series).each(function (i,s) {
				if (s.yAxis==null || $.inArray(s.yAxis, seenYAxisNumbers)<0) {
					seenYAxisNumbers.push(s.yAxis)
					yAxis.push({});
					realYAxis[s.yAxis] = x;
					s.yAxis = x;
					x++;
				}
				else {
					s.yAxis = realYAxis[s.yAxis];
				}
			});
			
			// Must have at least one yAxis
			if (yAxis.length==0) {
				yAxis.push({
					title: ""
				});
			}
			
			// Delay adding the series to the chart until they get data points, Highstocks is buggy
			seriesMeta = d.series;
			seriesMeta.forEach(function(meta) {
				meta.min = Infinity
				meta.max = -Infinity
			})

			chartTitle = d.title;
			
			// Init later
			destroyChart();
			
			// Remove csv buttons
			prot.body.find("div.csvDownload").remove();
		}
		
		
		// New series message
		else if (d.type=="s") {
			var s = d.series;
			if (chart != null && (s.yAxis==null || s.yAxis+1>chart.yAxis.length)) {
				// yAxis is read-only, I don't think this works
				chart.yAxis.push({
					title: s.name,
					opposite: (chart.yAxis.length%2==0)	
				});
				s.yAxis = chart.yAxis.length-1;
			}
			
			// Delay adding the series to the chart until they get data points, Highstocks is buggy
			if (chart==null || s.data==null || s.data.length<2) {
				while (seriesMeta.length < s.idx + 1)
					seriesMeta.push({});
				
				seriesMeta[s.idx] = s;
				s.min = Infinity
				s.max = -Infinity
			}
			else chart.addSeries(s,true,false);
		}
		
		// Day break
		else if (d.type=="b") {
			if (chart && chart.series) {
				for (var i=0;i<chart.series.length;i++) {
					// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
					chart.series[d.s].addPoint([maxTime+1, null],false,false,false);
				}
			}
		}
		
		// CSV file download link
		else if (d.type=="csv") {
			var div = $("<span class='csvDownload'></span>");
			var link = $("<a href='"+d.link+"'></a>");
			link.append("<img src='../images/download.png'/>&nbsp;"+d.filename);
			div.append(link);
			prot.body.append(div);
			div.effect("highlight",{},2000);
			
			link.click(function(event) {
				event.preventDefault();
				$.getJSON("existsCsv", {filename:d.filename}, (function(div) {
					return function(resp) {
						if (resp.success) {
							$(div).remove();
							var elemIF = document.createElement("iframe"); 
							elemIF.src = "downloadCsv?filename="+resp.filename; 
							elemIF.style.display = "none"; 
							document.body.appendChild(elemIF);
						}
						else alert("The file is already gone from the server. Please re-run your signal path!")
					}})(div));
			});
		}
		
	}
	
	pub.endOfResponses = function() {
		if (chart != null) {
			redrawChart(true)
		}
	}
	
	function redrawChart(scrollToEnd) {
		var extremes = chart.xAxis[0].getExtremes();
		
		var mx = (range==null || scrollToEnd ? maxTime : extremes.max);
		if (mx - minTime < range)
			mx = Math.min(maxTime, minTime + range);
		
		var mn = (range==null ? minTime : Math.max(minTime,mx-range));
		
		chart.xAxis[0].setExtremes(mn,mx,false,false);
		
		chart.redraw();
	}
	
	var superClean = pub.clean;
	pub.clean = function() {
		superClean();
		destroyChart();

		seriesMeta = [];
		realYAxis = [];
		minTime = null;
		maxTime = null;
		
		prot.body.find("div.csvDownload").remove();
	}
	
	var superUpdateFrom = pub.updateFrom;
	pub.updateFrom = function(data) {
		destroyChart();
		area = null;
		
		seriesMeta = [];
		realYAxis = [];
		minTime = null;
		maxTime = null;
		
		superUpdateFrom(data);
	}
	
	/**
	 * On start, bind connected inputs to series indices. We need
	 * to know which input results in which series.
	 */
	$(SignalPath).on("started", function() {
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
	})
	
	/**
	 * On SignalPath stopped, check that all series are shown properly in relation to chart yaxis range
	 */
	$(SignalPath).on("stopped", function() {
		if (chart && chart.series.length > 1) {
			// Find connected inputs
			var connectedInputs = pub.getInputs().filter(function(input) {
				return input.isConnected()
			})
			
			for (var i=0; i<chart.series.length; i++) {
				var yAxisRange = chart.series[i].yAxis.getExtremes().max - chart.series[i].yAxis.getExtremes().min
				var seriesRange = seriesMeta[i].max - seriesMeta[i].min
				
				// If series range is less than 10% of axis range, show a tip
				if (seriesRange/yAxisRange < 0.1) {
					var $input = connectedInputs[i] 
					$input.div.data("spObject").showYAxisWarning(chart.series[i].name)
				}
			}
		}
	})
	
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
			container: "#"+SignalPath.options.canvas,
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
	
	pub.showYAxisWarning = function(seriesName) {
		$yAxisSelectorButton.popover({
			content: "Series "+seriesName+" may not be showing properly. Use the Y-axis selection button to set its Y-axis.",
			placement: "right",
			trigger: "manual"
		})
		$yAxisSelectorButton.popover('show')
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