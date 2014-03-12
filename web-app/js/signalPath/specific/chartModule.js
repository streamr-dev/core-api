SignalPath.ChartModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.GenericModule(data,canvas,my)

	my.enableIONameChange = false;
	
	var area = null;
	
	var chart = null;
	var chartTitle = null;
	var yAxis = null;
	var minTime = null;
	var maxTime = null;
	
	var realYAxis = [];
	
	var seriesMeta = [];
	
//	var str = "";
	
	var range = null;
	
//	var handlingMessages = false;
	
	function resizeChart(moduleWidth,moduleHeight) {
		if (area!=null) {
			area.css("width",moduleWidth - 150);
			area.css("height",moduleHeight - 100);
			if (chart!=null) {
				chart.setSize(moduleWidth - 150, moduleHeight - 100, false);
			}
		}
	}
	
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		// Change context menu on Double inputs
		if (!my.jsonData.disableAxisSelection) {
			my.div.find("div.input.Double").removeClass("default-context-menu").addClass("chart-context-menu");
		}
		
		// Bind yAxis selection possibility to TimeSeries inputs
//		if (!my.jsonData.disableAxisSelection) {
//			my.div.find("div.input.Double").each(function(i,input) {
//				if (!$(input).hasClass("parameter")) {
//
//					// Unbind anything
//					$(input).click(
//							(function(input,my) {
//								return function() {
//									var n = $(input).find("span.ioname").text();
//
//									// Find input from json data
//									var jsonInput = null;
//									$(my.jsonData.inputs).each(function(i,jsI) {
//										if (jsI.name==n)
//											jsonInput=jsI;
//									});
//
//									var currentInput = "0";
//									if (jsonInput!=null && jsonInput.yAxis!=null) {
//										currentInput = jsonInput.yAxis;
//									}
//									var yAxis = prompt("Axis number for "+n+":",currentInput);
//									if (yAxis != null)
//										jsonInput.yAxis = parseInt(yAxis);
//								}
//							})(input,my)
//					);
//				}
//			});
//		}
		
		initArea();
		
		my.initResizable({
			minWidth: parseInt(my.div.css("min-width").replace("px","")),
			minHeight: parseInt(my.div.css("min-height").replace("px","")),
			stop: function(event,ui) {
				resizeChart(ui.size.width, ui.size.height);
			}
		});
	}
	my.createDiv = createDiv;	
	
	function getChart() {
		return chart;
	}
	my.getChart = getChart;
	
	function initArea() {
		my.body.find(".ioTable").css("width","0px");
		
		// Find the chart draw area
		area = my.body.find(".chartDrawArea");
		if (area==null || area.length==0) {
			// Add the range buttons
			var buttonDiv = $("<div class='chartRangeButtons'></div>");
			var buttonConfig = [{name:"1s",range:1*1000},{name:"15s",range:15*1000},{name:"1m",range:60*1000},{name:"15m",range:15*60*1000},{name:"30m",range:30*60*1000},{name:"1h",range:60*60*1000},{name:"2h",range:2*60*60*1000},{name:"4h",range:4*60*60*1000},{name:"1d",range:12*60*60*1000},{name:"All",range:null}];
			createRangeButtons(buttonDiv,buttonConfig);
			my.body.append(buttonDiv);
			
			// Add the series hide/show links
			var hideLink = $("<a class='seriesLink' href='#'>hide all</a>");
			buttonDiv.append(hideLink);
			hideLink.click(function() {
				if (chart!=null) {
					$.each(chart.series, function(index, series) {
						series.setVisible(false,false);
					});
					chart.redraw();
				}
				return false;
			});
			var showLink = $("<a class='seriesLink' href='#'>show all</a>");
			buttonDiv.append(showLink);
			showLink.click(function() {
				if (chart!=null) {
					$.each(chart.series, function(index, series) {
						series.setVisible(true,false);
					});
					chart.redraw();
				}
				return false;
			});
			
			// Create the chart area
			var areaId = "chartArea_"+(new Date()).getTime();
			area = $("<div id='"+areaId+"' class='chartDrawArea'></div>");
			my.body.append(area);
			
			resizeChart(my.div.width(),my.div.height());
		}
	}
	
	var superGetContextMenu = my.getContextMenu;
	my.getContextMenu = function(div) {
		var menu = superGetContextMenu(div);
		if (div.hasClass("ioname")) {
			menu.push({title: "Set Y-axis", cmd: "yaxis"});
		}
		return menu;
	};
	
	var superHandleContextMenuSelection = my.handleContextMenuSelection;
	my.handleContextMenuSelection = function(div,data,selection,event) {
		if (selection=="yaxis") {
			var n = $(div).find(".ioname").text();
			var yAxis = prompt("Axis number for "+n+":",data.yAxis);
			if (yAxis != null)
				data.yAxis = parseInt(yAxis);
			event.stopPropagation();
		}
		else superHandleContextMenuSelection(div,data,selection,event);
	};
	
	function createRangeButtons(buttonDiv,config) {
		for (var i=0;i<config.length;i++) {
			var c = config[i];
			var button = $("<button>"+c.name+"</button>");
			button.click((function(r) {
				return function() {
					range = r;
					redrawChart();
				};
			})(c.range));
			button.button();
			buttonDiv.append(button);
		}
	}
	
	function destroyChart() {
		if (chart!=null) {
			chart.destroy();
			my.body.find("button.yAxis").remove();
			chart=null;
		}
	}
	
	function initChart(title,series,yAxis) {
		
		destroyChart();

		$(area).show();
		
		// Dragging in the chartDrawArea must not move the module
		my.div.draggable("option", "cancel", ".chartDrawArea");
		
		if (yAxis==null) {
			yAxis = { 
//				title: {
//					text: null
//				}
			};
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

//		if (!series.dataLabels) {
//			series.dataLabels = {
//					enabled: true,
//					align: 'left',
//					x: 5,
//					y: 4,
//					formatter: function() {
//						if (this.point.x == this.series.data.length - 1) {
//							return this.y;
//						} else {
//							return null;
//						}
//					}
//			}
//		}
		
		
		
		var opts = {
				chart: {
					animation: false,
					renderTo: area.attr("id"),
					panning: true,
					spacingBottom: 40,
					backgroundColor: null
				},

				credits: {
					enabled: false
				},
				
//				title: {
//					text: (title ? title : "")
//				},
//
				xAxis: {
					ordinal: false
//					range: 60*1000
				},

				yAxis: yAxis, 

//				plotOptions: {
//					line: {
//						marker: {
//							enabled: false
//						},
////						dataGrouping: {
////							
////						}
//					}
//				},
//				
				legend: {
					enabled: true,
					maxHeight: 100,
					y: 40
				},
				
				rangeSelector: {
					enabled: false
//					buttons: [{
//						count: 1,
//						type: 'minute',
//						text: '1m',
//						millis: 1*60*1000
//					}, 
//					{
//						count: 15,
//						type: 'minute',
//						text: '15m',
//						millis: 15*60*1000
//					}, 
//					{
//						count: 60,
//						type: 'minute',
//						text: '1h',
//						millis: 1*60*60*1000
//					}, 
//					{
//						count: 1,
//						type: 'day',
//						text: '1d'
//					}, 
//					{
//						type: 'all',
//						text: 'All'
//					}],
//					inputEnabled: false,
//					selected: 2
				},
//				
//				exporting: {
//					enabled: false
//				},
//				
				navigator: {
					enabled: false
				},
				
//				scrollbar
				
				series: series
		};

		opts = $.extend(true, {}, SignalPath.defaultChartOptions || {}, opts);
		
		// Create the chart	
		chart = new Highcharts.StockChart(opts);
		
		// Append a button for yAxis setup
//		if (module.find("button.yAxis").length==0) {
//			var btn = $("<button class='yAxisConfig'>yAxes</button>");
//			module.append(btn);
//			
//			var yAxisDiv = $("<div class='yAxisConfig' style='display:none'></div");
//			module.append(yAxisDiv);
//			
//			$(series).each(function(i,s) {
//				yAxisDiv.append("<p>"+s.name+" - "+s.yAxis+"</p>");
//			});
//			var updateBtn = $("<button>Update</button>");
//			yAxisDiv.append(updateBtn);
//			
//			btn.click(
//					(function(div,c) {
//						return function() {
//							div.show();
//						}
//					})(yAxisDiv,chart)
//			);
//			
//			updateBtn.click(
//					(function(div,c) {
//						return function() {
//							div.hide();
//							var newY = chart.yAxis;
//							newY.splice(1,1);
//							chart.series[1].yAxis = 0;
//							initChart(chart.title,chart.series,newY);
//						}
//					})(yAxisDiv,chart)	
//			);
//		}
	}
	
	that.receiveResponse = function(d) {
//		handlingMessages = true;
		
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
//					seriesMeta[d.s].data = [{x:d.x, y:d.y}];
					seriesMeta[d.s].data = [[d.x, d.y]];
				}
				// Init the chart when any of the series gets it's 2nd data point
				else if (seriesMeta[d.s].data.length>=1) {
					// Add one more point to prevent coming here again
					// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
//					seriesMeta[d.s].data.push({x:d.x, y:d.y});
					seriesMeta[d.s].data.push([d.x, d.y]);


					// Init all other series to have at least two points
					for (var i=0;i<seriesMeta.length;i++) {
						if (i!=d.s) {
							// Add null points if no data received yet
							if (seriesMeta[i].data==null) {
								seriesMeta[i].data = [];
								// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
//								seriesMeta[i].data.push({x:seriesMeta[d.s].data[0].x, y:null});
								seriesMeta[i].data.push([seriesMeta[d.s].data[0][0], null]);
//								seriesMeta[i].data.push({x:d.x,y:null});
								seriesMeta[i].data.push([d.x,null]);
							}
							// If one point received, repeat first value
							else if (seriesMeta[i].data.length==1) {
								// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
//								seriesMeta[i].data.push({x:d.x, y:seriesMeta[i].data[0].y});
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
//				if (d.y!=null) {
				
				if (d.s<chart.series.length) {
					// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
//					chart.series[d.s].addPoint({x:d.x, y:d.y},false,false,false);
					chart.series[d.s].addPoint([d.x, d.y],false,false,false);
				}
				// Are there pending series adds in seriesMeta?
				else {
					if (seriesMeta[d.s].data==null)
						seriesMeta[d.s].data = [];
					
					// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
//					seriesMeta[d.s].data.push({x:d.x, y:d.y});
					seriesMeta[d.s].data.push([d.x, d.y]);
					
					for (var i=chart.series.length;i<seriesMeta.length;i++) {
						// Unfortunately we need to add series in order
						if (seriesMeta[i].data!=null && seriesMeta[i].data.length>1)
							chart.addSeries(seriesMeta[i],true,false);
						else break;
					}
				}
				
				
				
//					str += "chart.series[0].addPoint({x:"+d.x+",y:"+d.y+"},false,false,false);\n";
//				}
					
				// Alternative: DOES NOT WORK WITH HIGHSTOCK
//				var series = chart.series[d.s];
//				var point = (new series.pointClass()).init(series, {x:d.x, y:d.y});
//				series.data.push(point);
//				series.isDirty = true;

				// xAxis range can be set in millis
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
//						title: {
//							text:s.name
//						},
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
			my.body.find("div.csvDownload").remove();
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
//					chart.series[d.s].addPoint({x:maxTime+1, y:null},false,false,false);
					chart.series[d.s].addPoint([maxTime01, null],false,false,false);
				}
			}
		}
		
		// CSV file download link
		else if (d.type=="csv") {
			var div = $("<span class='csvDownload'></span>");
			var link = $("<a href='"+d.link+"'></a>");
			link.append("<img src='../images/download.png'/>&nbsp;"+d.filename);
			div.append(link);
			my.body.append(div);
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
	
	that.endOfResponses = function() {
//		handlingMessages = false;
		
		if (chart != null) {
			redrawChart(true);
//			str += "chart.redraw();\n";
			
//			if (chart.series!=null && chart.series.length>0) {
//				for (var i=0;i<chart.series.length;i++) {
//					var extremes = chart.series[i].xAxis.getExtremes();
//					console.info("i: "+i+", dataMax: "+extremes.dataMax+", dataMin: "+extremes.dataMin+", max: "+extremes.max+", min: "+extremes.min);
//				}
//			}
			
//			console.info(str);
		}
	}
	
	function redrawChart(scrollToEnd) {
		var extremes = chart.xAxis[0].getExtremes();
		
		var mx = (range==null || scrollToEnd ? maxTime : extremes.max);
		if (mx - minTime < range)
			mx = Math.min(maxTime, minTime + range);
		
		var mn = (range==null ? minTime : Math.max(minTime,mx-range));
		
//		for (var i=0;i<chart.series.length;i++)
//			chart.series[i].xAxis.setExtremes(mn,mx,false,false);
		
		chart.xAxis[0].setExtremes(mn,mx,false,false);
		
		chart.redraw();
	}
	
	var superClean = that.clean;
	that.clean = function() {
		superClean();
		destroyChart();

		seriesMeta = [];
		realYAxis = [];
		minTime = null;
		maxTime = null;
		
		my.body.find("div.csvDownload").remove();
	}
	
	var superUpdateFrom = that.updateFrom;
	that.updateFrom = function(data) {
		destroyChart();
		area = null;
		
		seriesMeta = [];
		realYAxis = [];
		minTime = null;
		maxTime = null;
		
		superUpdateFrom(data);
	}
	
	return that;
}

SignalPath.HighStocksModule = SignalPath.ChartModule;