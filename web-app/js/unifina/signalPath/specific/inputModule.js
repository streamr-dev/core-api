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

		$(SignalPath).on("started", function() {
			if(widget.enable)
				widget.enable()
			$(widget).trigger("started")
			$(pub).trigger("started")
		})

		$(SignalPath).on("stopped", function() {
			if(widget.disable)
				widget.disable()
			$(widget).trigger("stopped")
			$(pub).trigger("stopped")
		})

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
		$(pub).on("started", requestState)
	}

	function requestState (e, runData){
		if (!runData || !runData.adhoc) {
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
	
	return pub;
}
