SignalPath.MapModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.UIChannelModule(data,canvas,prot)

	var container = null
	var map = null

	prot.enableIONameChange = false;

	prot.getMap = function() {
		return map
	}
	
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();

        prot.div.addClass('map-module')

		container = $("<div class='map-container content'></div>")
		prot.body.width(500)
		prot.body.height(400)
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
			minWidth: 350,
			minHeight: 250,
			stop: updateSize
		});
	}
	prot.createDiv = createDiv;	
	
	function updateSize() {
		if (map) {
			map.redraw()
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
