SignalPath.Output = function(json, type, parentDiv, module, my) {
	my = my || {};
	my = new Endpoint(json,type,parentDiv,module,my);
	
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
	}
	
	var super_getJSPlumbEndpointOptions = my.getJSPlumbEndpointOptions;
	my.getJSPlumbEndpointOptions = function(json,connDiv) {
		var opts = super_getJSPlumbEndpointOptions(json,connDiv);
		
		opts.maxConnections = -1; // unlimited
		opts.anchor = [1, 0.5, 1, 0, 15, 0];
		
		return opts;
	}
	
	return my;
}