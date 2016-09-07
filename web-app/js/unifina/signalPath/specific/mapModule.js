SignalPath.MapModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.UIChannelModule(data,canvas,prot)

	var $container = null
	var map = null

	prot.enableIONameChange = false;	
		
	// Dragging in the chart container or the controls must not move the module
	prot.dragOptions.cancel = ".map-container"

	prot.getMap = function() {
		return map
	}
	
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();

		prot.body.css("height", "100%")

		container = $("<div class='map-container' style='width: 500px; height: 400px;'></div>")
		prot.body.append(container)

		var mapOptions = {
			centerLat: prot.jsonData.centerLat,
			centerLng: prot.jsonData.centerLng,
			zoom: prot.jsonData.zoom
		}
		if (prot.jsonData.options) {
			Object.keys(prot.jsonData.options).forEach(function(key) {
				mapOptions[key] = prot.jsonData.options[key].value
			})
		}

		map = new StreamrMap(container, mapOptions)

		$(map).on("move", function(e, data) {
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
		if (map) {
			var width = container.parent().width()
			var height = container.parent().height() - container.parent().find(".ioTable").outerHeight() - 20
			map.resize(width, height)
		}
	}

	prot.receiveResponse = function(d) {
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

	var superToJSON = pub.toJSON;
	pub.toJSON = function() {
		prot.jsonData = superToJSON();
		var json = map.toJSON()
		$.extend(true, prot.jsonData, json)
		return prot.jsonData
	}

	pub.getMap = function() {
		return map;
	}
	
	return pub;
}
