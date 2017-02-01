SignalPath.HeatmapModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.UIChannelModule(data,canvas,prot)

	var container = null
	var heatmap = null

	prot.enableIONameChange = false;	
		
	// Dragging in the chart container or the controls must not move the module
	prot.dragOptions.cancel = ".heatmap-container"
	
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();
        
        prot.div.addClass('map-module heatmap-module')
        
		container = $("<div class='heatmap-container content'></div>")
		prot.body.width(500)
		prot.body.height(400)
		prot.body.append(container)

		var heatMapOptions = {}
		if (prot.jsonData.options) {
			Object.keys(prot.jsonData.options).forEach(function(key) {
				heatMapOptions[key] = prot.jsonData.options[key].value
			})
		}
		if (heatMapOptions.centerLat!==undefined && heatMapOptions.centerLng!==undefined)
			heatMapOptions.center = [heatMapOptions.centerLat, heatMapOptions.centerLng]

		heatmap = new StreamrHeatMap(container, heatMapOptions)

		$(heatmap).on("move", function(e, data) {
			$.each(data, function(k, v) {
				if(prot.jsonData.options[k] && prot.jsonData.options[k].value)
					prot.jsonData.options[k].value = v
			})
		})

		prot.initResizable({
			minWidth: 350,
			minHeight: 250,
			stop: updateSize
		});

	}
	prot.createDiv = createDiv;	
	
	function updateSize() {
		if (heatmap) {
			heatmap.redraw()
		}
	}

	prot.receiveResponse = function(d) {
		heatmap.handleMessage(d)
	}
	
	var superClean = pub.clean;
	pub.clean = function() {
		if (heatmap)
			heatmap.clear()
	}

	var super_redraw = pub.redraw
	pub.redraw = function() {
		super_redraw()
		updateSize()
	}
	
	return pub;
}
