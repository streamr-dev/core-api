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

		prot.body.css("height", "100%")
		
		container = $("<div class='heatmap-container' style='width: 500px; height: 400px;'></div>")
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
			minWidth: parseInt(prot.div.css("min-width").replace("px","")),
			minHeight: parseInt(prot.div.css("min-height").replace("px","")),
			stop: updateSize
		});

		$(SignalPath).on("loaded", updateSize)
	}
	prot.createDiv = createDiv;	
	
	function updateSize() {
		if (heatmap) {
			var width = container.parent().width()
			var height = container.parent().height() - container.parent().find(".ioTable").outerHeight() - 20
			heatmap.resize(width, height)
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
