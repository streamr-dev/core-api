SignalPath.Output = function(json, parentDiv, module, type, pub) {
	pub = pub || {};
	pub = SignalPath.Endpoint(json, parentDiv, module, type || "output", pub);
	
	var super_createSettings = pub.createSettings;
	pub.createSettings = function(div,data) {
		super_createSettings(div,data);
		
		var switchDiv = $("<div class='switchContainer showOnFocus'></div>");
		div.append(switchDiv);
		
		// NoRepeat. Default true. Add only for TimeSeries type
		if (data.type=="Double" && (data.canBeNoRepeat==null || data.canBeNoRepeat)) {
			var noRepeat = new SignalPath.IOSwitch(switchDiv, "noRepeat", {
				getValue: (function(d){
					return function() { return d.noRepeat; };
				})(data),
				setValue: (function(d){
					return function(value) { return d.noRepeat = value; };
				})(data),
				buttonText: function() { return "NR"; },
				tooltip: 'No repeat'
			});
		}

		div.bind("spConnect", (function(me) {
			return function(event, input) {
				me.json.connected = true;
				me.div.addClass("connected");
				if (me.json.targets==null) {
					me.json.targets = [input.getId()];
				}
				else if (me.json.targets.indexOf(input.getId())==-1) {
					me.json.targets.push(input.getId());
				}
			}
		})(pub));
		
		div.bind("spDisconnect", (function(me) {
			return function(event, input) {
				// delete input from me.json.targets
				var i = me.json.targets.indexOf(input.getId());
				if (i != -1) {
					me.json.targets.splice(i, 1);
				}
				
				// if targets is empty, set connected to false
				if (me.json.targets.length==0) {
					me.json.connected = false;
					me.div.removeClass("connected");
				}
			}
		})(pub));
	}
	
	var super_getJSPlumbEndpointOptions = pub.getJSPlumbEndpointOptions;
	pub.getJSPlumbEndpointOptions = function(json,connDiv) {
		var opts = super_getJSPlumbEndpointOptions(json,connDiv);
		
		opts.maxConnections = -1; // unlimited
		opts.anchor = [1, 0.5, 1, 0, 15, 0];
		opts.cssClass = (opts.cssClass || "") + " jsPlumb_output";
		
		return opts;
	}
	
	pub.connect = function(endpoint) {
		jsPlumb.connect({source: endpoint.jsPlumbEndpoint, target:pub.jsPlumbEndpoint});
	}
	
	pub.getConnectedEndpoints = function() {
		var result = [];
		var connections = jsPlumb.getConnections({target:pub.getId(), scope:"*"});
		$(connections).each(function(j,connection) {
			result.push($(connection.source).data("spObject"));
		});
		return result;
	}
	
	pub.refreshConnections = function() {
		if (pub.json.targets!=null) {
			$(pub.json.targets).each(function(j,targetId) {
				// Does this connection already exist?
				if (jsPlumb.getConnections({source:targetId, target:pub.getId(), scope:"*"}).length == 0) {
					// Get the endpoint
					var input = $("#"+targetId).data("spObject");
					if (input!=null)
						pub.connect(input);
					else console.log("Warning: output "+pub.getId()+" should be connected to "+targetId+", but the input was not found!");
				}
			});
		}
	}
	
	return pub;
}