SignalPath.Input = function(json, parentDiv, module, type, my) {
	my = my || {};
	my = SignalPath.Endpoint(json, parentDiv, module, type || "input", my);
	
	var super_createDiv = my.createDiv;
	my.createDiv = function() {
		var div = super_createDiv();

		div.bind("spConnect", (function(me) {
			return function(event, output) {
				me.json.connected = true;
				me.json.sourceId = output.getId();
				me.div.addClass("connected");
			}
		})(my));
		
		div.bind("spDisconnect", (function(me) {
			return function(event, output) {
				me.json.connected = false;
				delete me.json.sourceId;
				me.div.removeClass("connected");
			}
		})(my));
		
		// Add/remove a warning class to unconnected inputs
		if (!my.json.suppressWarnings) {
			my.jsPlumbEndpoint.setStyle(jQuery.extend({},jsPlumb.Defaults.EndpointStyle,{strokeStyle:"red"}));

			div.bind("spConnect", (function(me) {
				return function(event, output) {
					me.jsPlumbEndpoint.setStyle(jsPlumb.Defaults.EndpointStyle);
				}
			})(my));

			div.bind("spDisconnect", (function(me) {
				return function(event, output) {
					me.jsPlumbEndpoint.setStyle(jQuery.extend({},jsPlumb.Defaults.EndpointStyle,{strokeStyle:"red"}));
				}
			})(my));
		}
		return div;
	}
	
	var super_createSettings = my.createSettings;
	my.createSettings = function(div,data) {
		super_createSettings(div,data);
		
		// Driving input. Default true.
		
		var switchDiv = $("<div class='switchContainer showOnFocus'></div>");
		div.append(switchDiv);
		
		if (data.canBeDrivingInput==null || data.canBeDrivingInput) {
			var driving = $("<div class='ioSwitch ioSwitchInput drivingInput'>DR</div>");
			if (data.drivingInput==null)
				data.drivingInput = (div.hasClass("parameter") ? false : true);
			driving.addClass(data.drivingInput ? "ioSwitchTrue" : "ioSwitchFalse");
			switchDiv.append(driving);
			driving.click(function() {
				if (data.drivingInput) {
					data.drivingInput = false;
					driving.removeClass("ioSwitchTrue");
					driving.addClass("ioSwitchFalse");
				}
				else {
					data.drivingInput = true;
					driving.addClass("ioSwitchTrue");
					driving.removeClass("ioSwitchFalse");
				}
			});
		}
		
		// Initial value. Default null/off. Only valid for TimeSeries type
		if (data.type=="Double" && (data.canHaveInitialValue==null || data.canHaveInitialValue)) {
			var iv = $("<div class='ioSwitch ioSwitchInput initialValue'></div>");
			switchDiv.append(iv);
			
			var updateIv = function() {
				if (data.initialValue==null) {
					iv.html("IV");
					iv.removeClass("ioSwitchTrue");
					iv.addClass("ioSwitchFalse");
				}
				else if (data.initialValue==0) {
					iv.html("0");
					iv.addClass("ioSwitchTrue");
					iv.removeClass("ioSwitchFalse");
				}
				else if (data.initialValue==1) {
					iv.html("1");
					iv.addClass("ioSwitchTrue");
					iv.removeClass("ioSwitchFalse");
				}
			}
			
			updateIv();
			
			iv.click(function() {
				if (data.initialValue==1) {
					data.initialValue = null;
					updateIv();
				}
				else if (data.initialValue==null) {
					data.initialValue = 0;
					updateIv();
				}
				else if (data.initialValue==0) {
					data.initialValue = 1;
					updateIv();
				}
			});
		}
		
		// Feedback connection. Default false. Switchable for TimeSeries types.
		
		if (data.type=="Double" && (data.canBeFeedback==null || data.canBeFeedback)) {
			var fb = $("<div class='ioSwitch ioSwitchInput feedback'>FB</div>");
			if (data.feedback==null)
				data.feedback = false;
			fb.addClass(data.feedback ? "ioSwitchTrue" : "ioSwitchFalse");
			switchDiv.append(fb);
			fb.click(function() {
				if (data.feedback) {
					data.feedback = false;
					fb.removeClass("ioSwitchTrue");
					fb.addClass("ioSwitchFalse");
				}
				else {
					data.feedback = true;
					fb.addClass("ioSwitchTrue");
					fb.removeClass("ioSwitchFalse");
				}
			});
		}
	}
	
	var super_getJSPlumbEndpointOptions = my.getJSPlumbEndpointOptions;
	my.getJSPlumbEndpointOptions = function(json,connDiv) {
		var opts = super_getJSPlumbEndpointOptions(json,connDiv);
		
		opts.connectorOverlays[0][1].direction = -1;
		opts.maxConnections = 1;
		opts.anchor = [0, 0.5, -1, 0, -15, 0];
		
		return opts;
	}
	
	my.connect = function(endpoint) {
		jsPlumb.connect({source: my.jsPlumbEndpoint, target:endpoint.jsPlumbEndpoint});
	}
	
	my.getConnectedEndpoints = function() {
		var result = [];
		var connections = jsPlumb.getConnections({source:my.getId(), scope:"*"});
		$(connections).each(function(j,connection) {
			result.push($(connection.target).data("spObject"));
		});
		return result;
	}
	
	my.refreshConnections = function() {
		if (my.json.sourceId!=null) {
			var connectedEndpoints = my.getConnectedEndpoints();
			if (connectedEndpoints.length==0) {
				var endpoint = $("#"+my.json.sourceId).data("spObject");
				if (endpoint!=null)
					my.connect(endpoint);
				else console.log("Warning: input "+my.getId()+" should be connected to "+my.json.sourceId+", but the output was not found!");
			}
			else if (connectedEndpoints.length==1 && connectedEndpoints[0].getId()!=my.json.sourceId) {
				console.log("Warning: input "+my.getId()+" should be connected to "+my.json.sourceId+", but is connected to "+connectedEndpoints[0].getId()+" instead!");
			}
		}
	}
	
	my.toJSON = function() {
//		var connectedEndpoints = my.getConnectedEndpoints();
//
//		if (connectedEndpoints.length>0) {
//			my.json.connected = true;
//			my.json.id = my.getId();
//			my.json.sourceId = connectedEndpoints[0].getId();
//		}
//		else my.json.connected = false;

		return my.json;
	}
	
	return my;
}