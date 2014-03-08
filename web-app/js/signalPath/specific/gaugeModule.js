SignalPath.GaugeModule = function(data,canvas,my) {
	my = my || {};
	var that = SignalPath.GenericModule(data,canvas,my)
	
	var area = null;	
	var chart = null;
	
	var _min;
	var _min;
		
	function resizeChart(moduleWidth,moduleHeight) {
		if (area!=null) {
			var width = moduleWidth - 150;
			var height = moduleHeight - 120;
			area.css("width",width);
			area.css("height",height);
			if (chart!=null) {
				var gaugeWidth = Math.min(width,2*height);
				chart.setSize(gaugeWidth, gaugeWidth/2, false);
			}
		}
	}
	
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		initArea();
		
		my.div.resizable({
			minWidth: parseInt(my.div.css("min-width").replace("px","")),
			minHeight: parseInt(my.div.css("min-height").replace("px","")),
			helper: "chart-resize-helper",
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
		my.body.find(".paramTable").css("width","0px");
		
		// Find the chart draw area
		area = my.body.find(".chartDrawArea");
		if (area==null || area.length==0) {
			// Create the chart area
			var areaId = "chartArea_"+(new Date()).getTime();
			area = $("<div id='"+areaId+"' class='chartDrawArea'></div>");
			my.body.append(area);
			
			resizeChart(my.div.width(),my.div.height());
		}
	}
	
	function destroyChart() {
		if (chart!=null) {
			chart.destroy();
			chart=null;
		}
	}
	
	function initChart(value, min, max) {
		
		destroyChart();

		$(area).show();
		
		// Dragging in the chartDrawArea must not move the module
//		my.div.draggable("option", "cancel", ".chartDrawArea");
		
		_min = min;
		_max = max;
		
		$(area).highcharts({
			credits: {
				enabled: false
			},
		    chart: {
		        type: 'gauge',
		        backgroundColor: null,
	            spacingTop: 40,
	            spacingLeft: 40,
	            spacingRight: 40,
	            spacingBottom: 40
		    },
		
		    title: {
		        text: my.jsonData.options.title.value,
		        floating: true,
		        verticalAlign: 'bottom',
		        y: 30
		    },
		    
		    pane: [{
		        startAngle: -90,
		        endAngle: 90,
		        background: null,
		        center: ['50%', '93%'],
		        size: '150%'
		    }],	    		        
		
		    yAxis: [{
		        min: min,
		        max: max,
		        minorTickPosition: 'outside',
		        tickPosition: 'outside',
		        labels: {
		        	enabled: my.jsonData.options.labels.value,
		        	formatter: (my.jsonData.options.labelFormatter.value!="" ? eval(my.jsonData.options.labelFormatter.value) : function() { return this.value; }),
		        	rotation: 'auto',
		        	distance: 20
		        },
		        plotBands: [{
		            color: {
		            	linearGradient: { x1: 0, x2: 1, y1: 0, y1: 0 },
		                stops: [
		                    [0, 'rgb(200, 255, 200)'],
		                    [1, 'rgb(0, 150, 0)']
		                ]
		            },
		            from: min,
		            to: max,
		        	innerRadius: '100%',
		        	outerRadius: '105%'
		        }],
		        pane: 0,
		    }],
		    
		    plotOptions: {
		    	gauge: {
		    		dataLabels: {
		    			enabled: false
		    		},
		    		dial: {
		    			radius: '100%'
		    		}
		    	}
		    },
		    	
		
		    series: [{
		        data: [value],
		        yAxis: 0
		    }]
		
		});
		
		chart = $(area).highcharts();
		
		var opts = {
				credits: {
					enabled: false
				},
			    chart: {
			    	animation: false,
			        type: 'gauge',
			        renderTo: area.attr("id"),
					spacingBottom: 40
			    },
			    pane: {
			        startAngle: -100,
			        endAngle: 100,
			        background: null,
			        center: ['50%', '65%'],
			    },
			    yAxis: {
			        min: min,
			        max: max,
			        minorTickPosition: 'outside',
			        tickPosition: 'outside',
			        pane: 0,
			        title: {
			        	text: 'SOCIAL',
			        	y: 120
			        }
			    },
			    plotOptions: {
			    	gauge: {
			    		dataLabels: {
			    			enabled: false
			    		},
			    		dial: {
			    			radius: '100%'
			    		}
			    	}
			    },
			    series: {
			        data: [value],
			        yAxis: 0
			    }
		};

		opts = $.extend(true, {}, SignalPath.defaultGaugeOptions || {}, opts);
		
		// Create the chart	
//		chart = new Highcharts.Chart(opts);

	}
	
	that.receiveResponse = function(d) {
		
		if (area==null)
			initArea();
		
		// Data point message
		if (d.type=="init") {
			if (chart==null) {
				initChart(d.v,d.min,d.max,d.title);
			}
		}
		else if (d.type=="u") {
			// Update value
			if (d.v!=null) {
				var point = chart.series[0].points[0];
				point.update(d.v, false);
			}
			// Update min
			if (d.min!=null) {
				// TODO: getExtremes() does not exist - highcharts bug?
				//var extremes = chart.yAxis.getExtremes();
				_min = d.min;
//				chart.series[0].setExtremes(d.min, _max, false);
				chart.yAxis.min = d.min;
			}
			// Update max
			if (d.max!=null) {
				// TODO: getExtremes() does not exist - highcharts bug?
//				var extremes = chart.yAxis.getExtremes();
				_max = d.max;
//				chart.series[0].setExtremes(_min, d.max, false);
				chart.yAxis.max = d.max;
			}
		}
	}
	
	that.endOfResponses = function() {
		if (chart != null) {
			redrawChart();
		}
	}
	
	function redrawChart() {
		chart.redraw();
	}
	
	var superClean = that.clean;
	that.clean = function() {
		superClean();
		destroyChart();
	}
	
	var superUpdateFrom = that.updateFrom;
	that.updateFrom = function(data) {
		destroyChart();
		area = null;
		
		superUpdateFrom(data);
	}
	
	var superToJSON = that.toJSON;
	function toJSON() {
		var result = superToJSON();
		result.layout.width = $(my.div).css('width');
		result.layout.height = $(my.div).css('height');		
		return result;
	}
	that.toJSON = toJSON;
	
	return that;
}
