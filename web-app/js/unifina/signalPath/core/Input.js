SignalPath.Input = function(json, parentDiv, module, type, pub) {
	pub = pub || {};
	pub = SignalPath.Endpoint(json, parentDiv, module, type || "input", pub);
	
	var super_createDiv = pub.createDiv;
	pub.createDiv = function() {
		var div = super_createDiv();

		div.bind("spConnect", (function(me) {
			return function(event, output) {
				me.json.sourceId = output.getId();
			}
		})(pub));
		
		div.bind("spDisconnect", (function(me) {
			return function(event, output) {
				delete me.json.sourceId;
			}
		})(pub));
		
		return div;
	}
	
	var super_hasWarning = pub.hasWarning
	pub.hasWarning = function() {
		// No warning if has initialvalue
		return (pub.getInitialValue()===null || pub.getInitialValue()===undefined) && super_hasWarning()
	}

	var super_updateState = pub.updateState;
	pub.updateState = function(value) {
        if (value) {
            super_updateState('"' + value + '"');
        } else {
            super_updateState("");
        }
    }
	
	var super_createSettings = pub.createSettings;
	pub.createSettings = function(div,data) {
		super_createSettings(div,data);
		
		// Driving input. Default true.
		
		var switchDiv = $("<div class='switchContainer showOnFocus'></div>");
		div.append(switchDiv);

		// The flags must be appended in reverse order

		if (data.canConnect === false) {
			return;
		}

		// Feedback connection. Default false. Switchable for TimeSeries types.
		if (data.type=="Double" && (data.canBeFeedback==null || data.canBeFeedback)) {
			var feedback = new SignalPath.IOSwitch(switchDiv, "ioSwitch feedback", {
				getValue: (function(d){
					return function() { return d.feedback; };
				})(data),
				setValue: (function(d){
					return function(value) { return d.feedback = value; };
				})(data),
				buttonText: function() { return "FB"; },
				tooltip: 'Feedback connection'
			});
		}

		// Initial value. Default null/off. Only valid for TimeSeries type
		if (data.type=="Double" && (data.canHaveInitialValue==null || data.canHaveInitialValue)) {
			var iv = new SignalPath.IOSwitch(switchDiv, "ioSwitch initialValue", {
				getValue: (function(d){
					return function() { return d.initialValue; };
				})(data),
				setValue: (function(d){
					return function(value) {
						pub.updateState(value)
						return d.initialValue = value;
					};
				})(data),
				buttonText: function(currentValue) { return "IV" },
				tooltip: 'Initial value',
				isActiveValue: function(currentValue) {
					return currentValue != null;
				}
			});

			pub.updateState(iv.getValue())

			// Override click handler
			iv.click = function() {
				if (iv.getValue()==null) {
					bootbox.prompt({
						title: "Initial Value:",
						callback: function(result) {
							if (result != null) {
								iv.setValue(parseFloat(result))
								iv.update();
								iv.div.html(iv.buttonText());
							}
						},
						className: 'initial-value-dialog'
					})
				}
				else {
					iv.setValue(undefined);
					iv.update();
					iv.div.html(iv.buttonText());
				}
			}

			// Remove requiresConnection class on update if input has initial value
			if (pub.json.requiresConnection) {
				$(iv).on("updated", function(e) {
					if (iv.isActiveValue(iv.getValue()))
						pub.removeClass("warning")
					else if (!pub.isConnected())
						pub.addClass("warning")
				})
				iv.update()
			}
		}

		if (data.canToggleDrivingInput==null || data.canToggleDrivingInput) {
			var driving = new SignalPath.IOSwitch(switchDiv, "ioSwitch drivingInput", {
				getValue: (function(d){
					return function() { return d.drivingInput; };
				})(data),
				setValue: (function(d){
					return function(value) { return d.drivingInput = value; };
				})(data),
				buttonText: function() { return "DR"; },
				tooltip: 'Driving input'
			});
		}
	}
	
	function getInitialValue() {
		return pub.json.initialValue;
	}
	pub.getInitialValue = getInitialValue;
	
	var super_getJSPlumbEndpointOptions = pub.getJSPlumbEndpointOptions;
	pub.getJSPlumbEndpointOptions = function(json,connDiv) {
		var opts = super_getJSPlumbEndpointOptions(json,connDiv);
		
		opts.connectorOverlays[0][1].direction = -1;
		opts.maxConnections = 1;
		opts.anchor = [0, 0.5, -1, 0, -15, 0];
		opts.cssClass = (opts.cssClass || "") + " jsPlumb_input";
		
		return opts;
	}
	
	pub.connect = function(endpoint) {
		jsPlumb.connect({source: pub.jsPlumbEndpoint, target:endpoint.jsPlumbEndpoint});
	}

	pub.disconnect = function() {
		var connections = jsPlumb.getConnections({source:pub.getId(), scope:"*"});
		$(connections).each(function(j,connection) {
			jsPlumb.detach(connection)
		});
	}
	
	pub.getConnectedEndpoints = function() {
		var result = [];
		var connections = jsPlumb.getConnections({source:pub.getId(), scope:"*"});
		$(connections).each(function(j,connection) {
			result.push($(connection.target).data("spObject"));
		});
		return result;
	}
	
	pub.refreshConnections = function() {
		if (pub.json.sourceId!=null) {
			var connectedEndpoints = pub.getConnectedEndpoints();
			if (connectedEndpoints.length==0) {
				var endpoint = $("#"+pub.json.sourceId).data("spObject");
				if (endpoint!=null)
					pub.connect(endpoint);
				else console.log("Warning: input "+pub.getId()+" should be connected to "+pub.json.sourceId+", but the output was not found!");
			}
			else if (connectedEndpoints.length==1 && connectedEndpoints[0].getId()!=pub.json.sourceId) {
				console.log("Warning: input "+pub.getId()+" should be connected to "+pub.json.sourceId+", but is connected to "+connectedEndpoints[0].getId()+" instead!");
			}
		}
	}
	
	return pub;
}