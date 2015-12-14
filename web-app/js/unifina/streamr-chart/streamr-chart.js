var CHART
(function(exports) {

/**
 * Events:
 * - initialized
 * - updated
 * - destroyed
 */

function StreamrChart(parent, options) {
	var _this = this

	this.$parent = $(parent)

	this.options = $.extend({
		rangeDropdown: true,
		showHideButtons: true,
		init: undefined
	}, options || {})

	this.metaDataInitialized = false
	this.chart = null
	this.messageQueue = []

	this.seriesMeta = []
	this.realYAxis = [];	
	this.chartTitle = '';
	this.yAxis = null;

	this.navigatorSeries = null
	this.latestNavigatorTimestamp = null
	this.range = null
	this.minTime = null;
	this.maxTime = null;

	// Show/Hide all series buttons
	if (this.options.showHideButtons) {
		this.$parent.append('<div class="chart-series-buttons chart-show-on-run pull-right btn-group">' +
			'<button class="btn btn-default btn-sm show-all-series" '+
				'title="Show all series"><i class="fa fa-plus-circle"></i></button>'+
			'<button class="btn btn-default btn-sm hide-all-series" '+
				'title="Hide all series"><i class="fa fa-minus-circle"></i></button>'+
		'</div>')

		var showHide = function(doShow) {
			return function() {
				if (!_this.chart)
					return;

				_this.seriesMeta.forEach(function(series) {
					series.impl.setVisible(doShow, false)
				})
				_this.chart.redraw()
			}
		}
		$('button.hide-all-series', this.$parent).click(showHide(false))
		$('button.show-all-series', this.$parent).click(showHide(true))
	}

	// Range dropdown
	if (this.options.rangeDropdown) {
		var $rangeSelector = $("<select class='chart-range-selector chart-show-on-run form-control pull-right' title='Range'></select>");
		this.rangeButtons = [
			{text:"All", type: "all"},
			{text:"month",count:30*24*60*60*1000, type: "millisecond"},
			{text:"week",count:7*24*60*60*1000, type: "millisecond"},
			{text:"day",count:24*60*60*1000, type: "millisecond"},
			{text:"12 h",count:12*60*60*1000, type: "millisecond"},
			{text:"8 h",count:8*60*60*1000, type: "millisecond"},
			{text:"4 h",count:4*60*60*1000, type: "millisecond"},
			{text:"2 h",count:2*60*60*1000, type: "millisecond"},
			{text:"30 min",count:30*60*1000, type: "millisecond"},
			{text:"1 h",count:60*60*1000, type: "millisecond"},
			{text:"15 min",count:15*60*1000, type: "millisecond"},
			{text:"1 min",count:60*1000, type: "millisecond"},
			{text:"15 sec",count:15*1000, type: "millisecond"},
			{text:"1 sec",count:1*1000, type: "millisecond"}
		]

		$.each(this.rangeButtons, function(i, c){
			var $option =  $("<option value='"+i+"'>"+c.text+"</option>")
			$rangeSelector.append($option)
		})

		$rangeSelector.on('change', function(e) {
			_this.range = _this.rangeButtons[$(this).val()].count
			_this.chart.rangeSelector.clickButton($(this).val())
		})

		this.$parent.append($rangeSelector);
	}

	$(this).on("initialized", function() {
		_this.$parent.find(".chart-show-on-run").show()
	})

	this.$parent.on('resize', function() {
		_this.resize()
	})

	$(window).on('resize', function() {
		_this.resize()
	})
	
	// Create the chart area
	var areaId = "chartArea_"+(new Date()).getTime()
	area = $("<div id='"+areaId+"' class='chartDrawArea'></div>")
	this.$parent.append(area)
	this.$area = area

	// An init message can be included in the options
	if (this.options.init) {
		this.initMetaData(this.options.init)
	}

}

StreamrChart.prototype.createHighstocksInstance = function(title, series, yAxis) {
	var _this = this

	this.$area.show();
	
	if (!yAxis) {
		yAxis = {};
	}
	else if ($.isArray(yAxis)) {
		for (var i=0;i<yAxis.length;i++)
			yAxis[i] = this.createYAxisOptions(yAxis[i], i);
	}

	Highcharts.setOptions({
		global: {
			useUTC: true
		}
	});
	
	var opts = {
		chart: {
			animation: false,
			renderTo: this.$area[0],
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

		rangeSelector: {
			enabled: true,
			buttons: _this.rangeButtons,
			inputEnabled: false,
			height: 0
		},

		yAxis: yAxis, 

		legend: {
			enabled: true
		},

		navigator: {
			enabled: true,
			series: $.extend(true, {type: 'line', step:true}, series[0])
		},
		
		plotOptions: {
			series: {
				animation: false
			}
		},
		
		scrollbar: {
			enabled: false
		},
		
		series: series
	};

	opts = $.extend(true, {}, this.options, opts);
	
	// Create the chart	
	this.chart = CHART = new Highcharts.StockChart(opts);
	$(this.chart.rangeSelector.zoomText.element).remove();
	$.each(this.chart.rangeSelector.buttons,function(i,b){
		$(b.element).remove();
	});
	
	// Collect pointers to actual series objects into seriesMeta[i].impl
	// This helps in indexkeeping, as the navigator series is appended
	// to chart.series
	this.navigatorSeries = this.chart.series[this.chart.series.length - 1]
	for (var i=0; i<this.seriesMeta.length; i++) {
		this.seriesMeta[i].impl = this.chart.series[i]
	}
	
	// Request animation frame to update the chart whenever it's updated
	$(this).on("updated", function() {
		if (!_this.animationRequest) {
			_this.animationRequest = window.requestAnimationFrame(function() {
				_this.redraw(true)
			})
		}
	})
	
	$(this).trigger("initialized")
}

StreamrChart.prototype.redraw = function(scrollToEnd) {
	this.animationRequest = null

	if (this.chart) {
		//var extremes = this.chart.xAxis[0].getExtremes();
        //
		//var mx = (this.range==null || scrollToEnd ? this.maxTime : extremes.max);
		//if (mx - this.minTime < this.range)
		//	mx = Math.min(this.maxTime, this.minTime + this.range);
        //
		//var mn = (this.range==null ? this.minTime : Math.max(this.minTime, mx - this.range));
        //
		//this.chart.xAxis[0].setExtremes(mn,mx,false,false);
		
		this.chart.redraw();
	}
}

StreamrChart.prototype.resize = function(moduleWidth, moduleHeight) {
	if (!this.$area)
		return;

	if (moduleWidth && moduleHeight) {
		var w = moduleWidth - 100
		var h = moduleHeight - 110

		this.$area.css('width', w)
		this.$area.css('height', h)

		if (this.chart)
			this.chart.setSize(w, h, false)
	}
	else if (this.chart) {
		this.chart.setSize(this.$area.width(), this.$area.height(), false)
	}
}

StreamrChart.prototype.getSeriesMetaData = function() {
	return this.seriesMeta
}

StreamrChart.prototype.setYAxis = function(seriesIndex, yx) {
	// Add a new yAxis to the chart if necessary
	if (this.realYAxis[yx]===undefined) {
		var newAxis = this.createYAxisOptions({}, this.yAxis.length)
		this.realYAxis[yx] = this.chart.yAxis.length
		this.yAxis.push(newAxis)
		this.chart.addAxis(newAxis, false)
	}
	
	this.seriesMeta[seriesIndex].impl.update({yAxis: this.realYAxis[yx]}, true)
}

StreamrChart.prototype.createYAxisOptions = function(options, n) {
	var result = $.extend({
		opposite: (n%2==0),
		offset: (Math.floor(n/2)+1)*30,
		title: {
			text: ""
		}
	}, this.options.yAxis || {}, options)
	return result
}

StreamrChart.prototype.destroy = function() {
	if (this.chart) {
		this.chart.destroy()
		this.$parent.find("button.yAxis").remove()
		this.chart = null
	}

	this.metaDataInitialized = false;
	this.messageQueue = [];
	this.seriesMeta = [];
	this.navigatorSeries = null
	this.latestNavigatorTimestamp = null
	this.realYAxis = [];
	this.minTime = null;
	this.maxTime = null;

	this.$parent.find(".chart-show-on-run").hide()
	$(this).trigger('destroyed')
}

StreamrChart.prototype.pushDataToMetaSeries = function(meta, d) {
	// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
	if (!meta.data) {
		meta.data = [[d.x, d.y]]
		if (d.y!=null) {
			meta.min = d.y
			meta.max = d.y
		}
	}
	else {
		meta.data.push([d.x, d.y])
		if (d.y!=null) {
			if (meta.min > d.y)
				meta.min = d.y
			if (meta.max < d.y)
				meta.max = d.y
		}
	}
}

StreamrChart.prototype.initMetaData = function(d) {
	var _this = this

	// Never reinitialize
	if (!this.metaDataInitialized) {
		this.yAxis = d.yAxis || [];
		
		var x=0;
		var seenYAxisNumbers = []

		// Remap the "user" yAxis assignments to actual Highcharts ones,
		// which need to be ascending and without gaps
		$(d.series).each(function (i,s) {
			if (s.yAxis==null || $.inArray(s.yAxis, seenYAxisNumbers)<0) {
				seenYAxisNumbers.push(s.yAxis)
				_this.yAxis.push({});
				_this.realYAxis[s.yAxis] = x;
				s.yAxis = x;
				x++;
			}
			else {
				s.yAxis = _this.realYAxis[s.yAxis];
			}
		});
		
		// Must have at least one yAxis
		if (this.yAxis.length==0) {
			this.yAxis.push({
				title: ""
			});
		}
		
		// Delay adding the series to the chart until they get data points, Highstocks is buggy
		this.seriesMeta = d.series;
		this.seriesMeta.forEach(function(meta) {
			meta.min = Infinity
			meta.max = -Infinity
		})

		this.title = d.title;
		this.metaDataInitialized = true

		// If messageQueue contains messages, process them now
		this.messageQueue.forEach(function(msg) {
			_this.handleMessage(msg)
		})
		this.messageQueue = []
	}
}

StreamrChart.prototype.handleMessage = function(d) {
	var _this = this
	var seriesMeta = this.seriesMeta
	var realYAxis = this.realYAxis
	var chart = this.chart

	if (!this.metaDataInitialized && d.type!=="init") {
		this.messageQueue.push(d)
		return
	}

	// Data point message
	if (d.type=="p") {
		if (this.minTime==null || d.x!=null && d.x<this.minTime)
			this.minTime = d.x;
		if (this.maxTime==null || d.x!=null && d.x>this.maxTime)
			this.maxTime = d.x;
		
		// We need at least two points to init the chart, stash the first one in seriesMeta
		if (!chart) {
			this.pushDataToMetaSeries(seriesMeta[d.s], d)

			if (seriesMeta[d.s].data.length>=2) {
				// Init all other series to have at least two points
				for (var i=0;i<seriesMeta.length;i++) {
					if (i!=d.s) {
						// Add null points if no data received yet
						if (seriesMeta[i].data==null) {
							this.pushDataToMetaSeries(seriesMeta[i], {x:seriesMeta[d.s].data[0][0], y:null})
							this.pushDataToMetaSeries(seriesMeta[i], {x:d.x, y:null})
						}
						// If one point received, repeat first value
						else if (seriesMeta[i].data.length==1) {
							this.pushDataToMetaSeries(seriesMeta[i], {x:d.x, y:null})
						}
					}
				}
				
				this.createHighstocksInstance(this.chartTitle,seriesMeta,this.yAxis);
			}
		}
		// Chart has already been initialized
		else {
			// addPoint is slow?
			if (seriesMeta[d.s].impl) {
				// Changed to array format to avoid turboThreshold errors http://www.highcharts.com/errors/20
				seriesMeta[d.s].impl.addPoint([d.x, d.y],false,false,false);
				if (seriesMeta[d.s].min > d.y)
					seriesMeta[d.s].min = d.y
				if (seriesMeta[d.s].max < d.y)
					seriesMeta[d.s].max = d.y

				// Only add new points to the navigator if they are at least ten minutes apart
				if (d.s===0 && (!this.latestNavigatorTimestamp || d.x > this.latestNavigatorTimestamp + 60000)) {
					this.navigatorSeries.addPoint([d.x, d.y],false,false,false);
					this.latestNavigatorTimestamp = d.x
				}
			}
			// Come here if there are series that are not yet added to the chart (requires at least 2 data points)
			// We might come here after adding series to the chart on-the-fly
			else {
				this.pushDataToMetaSeries(seriesMeta[d.s], [d.x, d.y])

				// Find the first unadded series and see if it can be added
				// (Unfortunately series need to be added in order to avoid problems with Highcharts)
				for (var i=0;i<seriesMeta.length;i++) {
					if (!seriesMeta[i].impl) {
						if (seriesMeta[i].data!=null && seriesMeta[i].data.length>1)
							seriesMeta[i].impl = chart.addSeries(seriesMeta[i],false,false);
						break
					}
				}
			}
		}
	}
	
	// Init message, for backwards compatibility
	else if (d.type=="init") {
		this.initMetaData(d)
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
		else chart.addSeries(s,false,false);
	}
	
	// Day break
	else if (d.type=="b") {
		if (chart && chart.series && chart.series[d.s]) {
			chart.series[d.s].addPoint([this.maxTime+1, null],false,false,false);
		}
	}
	
	$(this).trigger('updated')
}

exports.StreamrChart = StreamrChart

})(typeof(exports) !== 'undefined' ? exports : window)
