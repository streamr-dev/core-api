SignalPath.MapModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var $container = null
	var map = null

	prot.enableIONameChange = false;	
		
	// Dragging in the chart container or the controls must not move the module
	prot.dragOptions.cancel = ".map-container"
	
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();

		prot.body.css("height", "100%")
		

		container = $("<div class='map-container' style='width: 500px; height: 400px;'></div>")
		prot.body.append(container)

		var mapOptions = {}
		if (prot.jsonData.options) {
			Object.keys(prot.jsonData.options).forEach(function(key) {
				mapOptions[key] = prot.jsonData.options[key].value
			})
		}
		if (mapOptions.centerLat!==undefined && mapOptions.centerLng!==undefined)
			mapOptions.center = [mapOptions.centerLat, mapOptions.centerLng]

		map = new StreamrMap(container, mapOptions)

		prot.initResizable({
			minWidth: parseInt(prot.div.css("min-width").replace("px","")),
			minHeight: parseInt(prot.div.css("min-height").replace("px","")),
			stop: updateSize
		});

		$(SignalPath).on("loaded", updateSize)
	}
	prot.createDiv = createDiv;	
	
	function updateSize() {
		if (map) {
			var width = container.parent().width()
			var height = container.parent().height() - container.parent().find(".ioTable").outerHeight() - 20
			map.resize(width, height)
		}
	}

	pub.receiveResponse = function(d) {
		map.handleMessage(d)
	}
	
	var superClean = pub.clean;
	pub.clean = function() {
		if (map)
			map.clear()
	}

	var super_redraw = pub.redraw
	pub.redraw = function() {
		super_redraw()
		updateSize()
	}
	
	return pub;
}
