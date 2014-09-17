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
			// Add the range buttons
			var buttonDiv = $("<div class='chartRangeButtons btn-group'></div>");
			var buttonConfig = [{name:"1s",range:1*1000},{name:"15s",range:15*1000},{name:"1m",range:60*1000},{name:"15m",range:15*60*1000},{name:"30m",range:30*60*1000},{name:"1h",range:60*60*1000},{name:"2h",range:2*60*60*1000},{name:"4h",range:4*60*60*1000},{name:"1d",range:12*60*60*1000},{name:"All",range:null}];
			createRangeButtons(buttonDiv,buttonConfig);
			prot.body.append(buttonDiv);

			prot.body.append('<div class="pull-right btn-group">' +
				'<button class="btn btn-default btn-sm show-all-series" '+
					'title="Show all series"><i class="fa fa-plus-circle"></i></button>'+
				'<button class="btn btn-default btn-sm hide-all-series" '+
					'title="Hide all series"><i class="fa fa-minus-circle"></i></button>'+
			'</div>')

			$('button.hide-all-series', prot.body).click(showHide(false))
			$('button.show-all-series', prot.body).click(showHide(true))
			
			// Create the chart area
			var areaId = "chartArea_"+(new Date()).getTime()
			area = $("<div id='"+areaId+"' class='chartDrawArea'></div>")
			prot.body.append(area)
			
			resizeChart(prot.div.width(), prot.div.height())
		}
	}
	
	var superGetContextMenu = prot.getContextMenu
	prot.getContextMenu = function(div) {
		var menu = superGetContextMenu(div)
		if (div.hasClass("ioname")) {
			menu.push({title: "Set Y-axis", cmd: "yaxis"})
		}
		return menu;
	}
	
	var superHandleContextMenuSelection = prot.handleContextMenuSelection;
	prot.handleContextMenuSelection = function(target, selection) {
		if (selection=="yaxis") {
			var n = $(target.div).find(".ioname").text();
			var yAxis = prompt("Axis number for "+n+":",target.json.yAxis);
			if (yAxis != null)
				target.json.yAxis = parseInt(yAxis);

			$(prot.div).trigger('yAxisChanged', n)
		}
		else superHandleContextMenuSelection(target, selection);
	};
	
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
	
	function initChart(title,series,yAxis) {
		destroyChart();

		$(area).show();

		// Dragging in the chartDrawArea must not move the module
		prot.div.draggable("option", "cancel", ".chartDrawArea");
		
		if (yAxis==null) {
			yAxis = {};
		}
		else if ($.isArray(yAxis)) {
			for (var i=0;i<yAxis.length;i++)
				yAxis[i] = $.extend({}, SignalPath.defaultChartOptions.yAxis || {}, yAxis[i]);
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
			// Init axis opposite valus
			for (var i=0;i<yAxis.length;i++) {
				if (yAxis[i].opposite==null) {
					yAxis[i].opposite = (x%2==0);
					x++;
				}
			}
			
			x=0;
			var left = 0;
			var right = 0;
			$(d.series).each(function (i,s) {
				if (s.yAxis==null || s.yAxis+1>yAxis.length) {
					yAxis.push({
						offset: ((x%2==0 ? left : right)+1)*30,
						opposite: (x%2==0)	
					});
					
					if (x%2==0)
						left++;
					else right++;
					
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
	
	return pub;
}

SignalPath.HighStocksModule = SignalPath.ChartModule;