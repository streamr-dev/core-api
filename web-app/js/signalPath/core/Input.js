SignalPath.Input = function(json, type, parentDiv, module, my) {
	my = my || {};
	my = new Endpoint(json,type,parentDiv,module,my);
	
	var super_createDiv = my.createDiv;
	my.createDiv = function(data, type, parentDiv, module) {
		var result = super_createDiv(data,type,parentDiv,module);

		// Add/remove a warning class to unconnected inputs
		if (!data.suppressWarnings) {
			div.data("endpoint").setStyle(jQuery.extend({},jsPlumb.Defaults.EndpointStyle,{strokeStyle:"red"}));

			div.bind("spConnect", (function(me) {
				return function(output) {
					div.data("endpoint").setStyle(jsPlumb.Defaults.EndpointStyle);
				}
			})(div));

			div.bind("spDisconnect", (function(me) {
				return function(output) {
					div.data("endpoint").setStyle(jQuery.extend({},jsPlumb.Defaults.EndpointStyle,{strokeStyle:"red"}));
				}
			})(div));
		}
		return result;
	}
	
	var super_createSettings = my.createSettings;
	my.createSettings = function(div,data) {
		super_createSettings(div,data);
		
		// Driving input. Default true.
		
		var switchDiv = $("<div class='switchContainer'></div>");
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
	
	return my;
}