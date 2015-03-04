SignalPath.HeatmapModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var $container = null
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

		heatmap = new StreamrHeatMap(container, {})

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

	pub.receiveResponse = function(d) {
		heatmap.handleMessage(d)
	}
	
	var superClean = pub.clean;
	pub.clean = function() {
		if (heatmap)
			heatmap.clear()
	}
	
	return pub;
}
