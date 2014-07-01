SignalPath.Input = function(json, parentDiv, module, type, pub) {
	pub = pub || {};
	pub = SignalPath.Endpoint(json, parentDiv, module, type || "input", pub);
	
	var super_createDiv = pub.createDiv;
	pub.createDiv = function() {
		var div = super_createDiv();

		div.bind("spConnect", (function(me) {
			return function(event, output) {
				me.json.connected = true;
				me.json.sourceId = output.getId();
				me.div.addClass("connected");
			}
		})(pub));
		
		div.bind("spDisconnect", (function(me) {
			return function(event, output) {
				me.json.connected = false;
				delete me.json.sourceId;
				me.div.removeClass("connected");
			}
		})(pub));
		
		// Add/remove a warning class to unconnected inputs
		if (!pub.json.suppressWarnings) {
			pub.jsPlumbEndpoint.setStyle(jQuery.extend({},jsPlumb.Defaults.EndpointStyle,{strokeStyle:"red"}));

			div.bind("spConnect", (function(me) {
				return function(event, output) {
					me.jsPlumbEndpoint.setStyle(jsPlumb.Defaults.EndpointStyle);
				}
			})(pub));

			div.bind("spDisconnect", (function(me) {
				return function(event, output) {
					me.jsPlumbEndpoint.setStyle(jQuery.extend({},jsPlumb.Defaults.EndpointStyle,{strokeStyle:"red"}));
				}
			})(pub));
		}
		return div;
	}
	
	var super_createSettings = pub.createSettings;
	pub.createSettings = function(div,data) {
		super_createSettings(div,data);
		
		// Driving input. Default true.
		
		var switchDiv = $("<div class='switchContainer showOnFocus'></div>");
		div.append(switchDiv);
		
		if (data.canToggleDrivingInput==null || data.canToggleDrivingInput) {
			var driving = new SignalPath.IOSwitch(switchDiv, "drivingInput", {
				getValue: (function(d){
					return function() { return d.drivingInput; };
				})(data),
				setValue: (function(d){
					return function(value) { return d.drivingInput = value; };
				})(data),
				buttonText: function() { return "DR"; },
			});
		}
		
		// Initial value. Default null/off. Only valid for TimeSeries type
		if (data.type=="Double" && (data.canHaveInitialValue==null || data.canHaveInitialValue)) {
			var iv = new SignalPath.IOSwitch(switchDiv, "initialValue", {
				getValue: (function(d){
					return function() { return d.initialValue; };
				})(data),
				setValue: (function(d){
					return function(value) { return d.initialValue = value; };
				})(data),
				buttonText: function() { 
					if (this.getValue()==null)
						return "IV";
					else return this.getValue().toString();
				},
				nextValue: function(currentValue) {
					if (currentValue==null)
						return 0;
					else if (currentValue==0)
						return 1;
					else return null;
				},
				isActiveValue: function(currentValue) {
					return currentValue != null;
				}
			});
		}
		
		// Feedback connection. Default false. Switchable for TimeSeries types.
		
		if (data.type=="Double" && (data.canBeFeedback==null || data.canBeFeedback)) {
			var feedback = new SignalPath.IOSwitch(switchDiv, "feedback", {
				getValue: (function(d){
					return function() { return d.feedback; };
				})(data),
				setValue: (function(d){
					return function(value) { return d.feedback = value; };
				})(data),
				buttonText: function() { return "FB"; },
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
		
		return opts;
	}
	
	pub.connect = function(endpoint) {
		jsPlumb.connect({source: pub.jsPlumbEndpoint, target:endpoint.jsPlumbEndpoint});
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
	
	pub.toJSON = function() {
//		var connectedEndpoints = pub.getConnectedEndpoints();
//
//		if (connectedEndpoints.length>0) {
//			pub.json.connected = true;
//			pub.json.id = pub.getId();
//			pub.json.sourceId = connectedEndpoints[0].getId();
//		}
//		else pub.json.connected = false;

		return pub.json;
	}
	
	return pub;
}