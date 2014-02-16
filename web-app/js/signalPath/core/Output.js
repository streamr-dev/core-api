SignalPath.Output = function(json, parentDiv, module, type, my) {
	my = my || {};
	my = SignalPath.Endpoint(json, parentDiv, module, type || "output", my);
	
	var super_createSettings = my.createSettings;
	my.createSettings = function(div,data) {
		super_createSettings(div,data);
		
		var switchDiv = $("<div class='switchContainer'></div>");
		div.append(switchDiv);
		
		// NoRepeat. Default true. Add only for TimeSeries type
		if (data.type=="Double" && (data.canBeNoRepeat==null || data.canBeNoRepeat)) {
			var noRepeat = $("<div class='ioSwitch ioSwitchOutput noRepeat'>NR</div>");
			if (data.noRepeat==null)
				data.noRepeat = true;
			noRepeat.addClass(data.noRepeat ? "ioSwitchTrue" : "ioSwitchFalse");
			switchDiv.append(noRepeat);
			noRepeat.click(function() {
				if (data.noRepeat) {
					data.noRepeat = false;
					noRepeat.removeClass("ioSwitchTrue");
					noRepeat.addClass("ioSwitchFalse");
				}
				else {
					data.noRepeat = true;
					noRepeat.addClass("ioSwitchTrue");
					noRepeat.removeClass("ioSwitchFalse");
				}
			});
		}

		// TODO: optimization: in load phase it is not necessary to check these
		div.bind("spConnect", (function(me) {
			return function(event, input) {
				me.json.connected = true;
				if (me.json.targets==null) {
					me.json.targets = [input.getId()];
				}
				else if (me.json.targets.indexOf(input.getId())==-1) {
					me.json.targets.push(input.getId());
				}
			}
		})(my));
		
		div.bind("spDisconnect", (function(me) {
			return function(event, input) {
				// delete input from me.json.targets
				var i = me.json.targets.indexOf(input.getId());
				if (i != -1) {
					me.json.targets.splice(i, 1);
				}
				
				// if targets is empty, set connected to false
				if (me.json.targets.length==0)
					me.json.connected = false;
			}
		})(my));
	}
	
	var super_getJSPlumbEndpointOptions = my.getJSPlumbEndpointOptions;
	my.getJSPlumbEndpointOptions = function(json,connDiv) {
		var opts = super_getJSPlumbEndpointOptions(json,connDiv);
		
		opts.maxConnections = -1; // unlimited
		opts.anchor = [1, 0.5, 1, 0, 15, 0];
		
		return opts;
	}
	
	my.connect = function(endpoint) {
		jsPlumb.connect({source: endpoint.jsPlumbEndpoint, target:my.jsPlumbEndpoint});
	}
	
	my.getConnectedEndpoints = function() {
		var result = [];
		var connections = jsPlumb.getConnections({target:my.getId(), scope:"*"});
		$(connections).each(function(j,connection) {
			result.push($(connection.source).data("spObject"));
		});
		return result;
	}
	
	my.refreshConnections = function() {
		if (my.json.targets!=null) {
			$(my.json.targets).each(function(j,targetId) {
				// Does this connection already exist?
				if (jsPlumb.getConnections({source:targetId, target:my.getId(), scope:"*"}).length == 0) {
					// Get the endpoint
					var input = $("#"+targetId).data("spObject");
					if (input!=null)
						my.connect(input);
					else console.log("Warning: output "+my.getId()+" should be connected to "+targetId+", but the input was not found!");
				}
			});
		}
	}
	
	my.toJSON = function() {
		return my.json;
	}
	
	return my;
}