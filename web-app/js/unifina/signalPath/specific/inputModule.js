SignalPath.InputModule = function(data,canvas,prot) {
	prot = prot || {};
	var pub = SignalPath.GenericModule(data,canvas,prot)

	var widget;
	
	var super_createDiv = prot.createDiv;
	prot.createDiv = function() {
		super_createDiv();
		if(!prot.jsonData.widget)
			throw "prot.jsonData.widget must be defined!"
		var widgetName = eval(prot.jsonData.widget)
		widget = new widgetName(
			prot.body,
			prot.jsonData
		)
		widget.render()

		$(SignalPath).on("started loaded", prot.start)

		$(SignalPath).on("loaded", prot.requestState)

		$(SignalPath).on("stopped", prot.stop)

		$(widget).on("update", function() {
			prot.redraw()
		})

		$(widget).on("input", function(e, value) {
			prot.sendValue(value)
		})

		if(widget.getDragCancelAreas !== undefined) {
			var list = prot.div.draggable("option", "cancel")
			widget.getDragCancelAreas().forEach(function(area) {
				list += "," + area
			})
			prot.div.draggable("option", "cancel", list)
		}
	}

	prot.start = function() {
		if (SignalPath.isRunning()) {
			if (widget.enable)
				widget.enable()
			$(widget).trigger("started")
		}
	}

	prot.stop = function() {
		if(widget.disable)
			widget.disable()
		$(widget).trigger("stopped")
		$(pub).trigger("stopped")
	}

	prot.requestState = function(e, json) {
		if (SignalPath.isRunning())
			requestState(json)
	}


	function requestState(json) {
		if (json && !json.adhoc) {
			SignalPath.sendRequest(prot.hash, {type:'getState'}, function(response) {
				widget.updateState(response.state)
			})
		}
	}

	var super_toJSON = pub.toJSON
	pub.toJSON = function () {
		prot.jsonData = super_toJSON()
		if (widget.toJSON)
			$.extend(prot.jsonData, widget.toJSON())
		return prot.jsonData
	}
	
	pub.receiveResponse = function(p) {
		if(widget.receiveResponse)
			widget.receiveResponse(p)
	}

	prot.sendValue = function(value) {
		SignalPath.sendRequest(pub.getHash(), {
			type: "uiEvent",
			value: value
		}, function(resp) {});
	}

	pub.onClose = function() {
		$(widget).off()
		$(SignalPath).off("started loaded", prot.start)
		$(SignalPath).off("loaded", prot.requestState)
	}
	
	return pub;
}
