SignalPath.Endpoint = function(json, type, parentDiv, module, my) {
	my = my || {
			json: json,
			type: type,
			parentDiv: parentDiv,
			module: module
		};
	
	function createDiv(data, type, parentDiv, module) {
		
		// Create connection div
		var div = $("<div class='"+type+" "+data.type+"'></div>");
		div.data("name",data.name);
		module.getDiv().data(type+"."+data.name,div);
		
		var ioname = $("<div class='ioname'>"+(data.displayName ? data.displayName : data.name)+"</span>");
		div.append(ioname);
		
		parentDiv.append(div);
		
		// Link json data to the div
		div.data("json",data);

		if (data.export)
			setExport(div,data,true);
		
		// TODO: endpointId is for backwards compatibility
		var id = (data.endpointId != null ? data.endpointId : data.id);
		createJSPlumbEndpoint(data, div, id);

		// Create io settings
		my.createSettings(div,data);
		
		// Bind context menu listeners
		if (!my.disableContextMenu) {
			div.addClass("context-menu");
		}
		
		div.on('spContextMenuSelection', (function(d,j) {
			return function(event,selection) {
				my.handleContextMenuSelection(d,j,selection,event);
			};
		})(div,data));
		
		div.trigger("spIOReady");
		
		return div;
	}
	my.createDiv = createDiv;
	
	my.createSettings = function(div,data) {
		alert("createSettings called in Endpoint!");
	}
	
	function setExport(iodiv,data,value) {
		if (value) {
			iodiv.addClass("export");
			data.export = true;
		}
		else {
			iodiv.removeClass("export");
			data.export = false;
		}
	}
	my.setExport = setExport;
	
	function toggleExport(iodiv,data) {
		my.setExport(iodiv,data,!data.export);
	}
	my.toggleExport = toggleExport;
	
	function generateId() {
		var id = "myId_"+my.module.getHash()+"_"+new Date().getTime();
		
		// Check for clashes
		while ($("#"+id).length>0)
			id += "_";
				
		return id;
	}	

	function getJSPlumbEndpointOptions(json,connDiv) {
		return {
			isSource:true, 
			isTarget:true,
			dragOptions: {},
			dropOptions: {},
			beforeDrop: function(info) {
				return my.checkConnection(info) && my.checkConnectionDirection(info);
			},
			connectorOverlays: [["Arrow", {direction:1, paintStyle: {cssClass:"arrow"}}]];
		};
	}
	my.getJSPlumbEndpointOptions = getJSPlumbEndpointOptions;
	
	function createJSPlumbEndpoint(json,connDiv,id) {
		// Generate custom id for the div if using the new jsPlumb ids
		if (!id || id.length<20) {
			var oldId = id;
			id = generateId();
			
			if (oldId)
				SignalPath.replacedIds[oldId] = id;
		}
		
		connDiv.attr("id",id);
		
		var isOutput = connDiv.hasClass('output');
		var isInput = connDiv.hasClass('input');
	
//		my.positionConnections();
		
		// For connections
		// TODO: define overlay in theme?
		var overlays = [["Arrow", {direction:(isInput ? -1 : 1), paintStyle: {cssClass:"arrow"}}]];
		
		var ep = jsPlumb.addEndpoint(connDiv, my.getJSPlumbEndpointOptions(json.connDiv));
		ep.bind("click", function(endpoint) {
			console.log(endpoint);
		});
		
		$(connDiv).data("endpoint",ep);
		$(connDiv).data("acceptedTypes", (json.acceptedTypes!=null ? json.acceptedTypes : [json.type]));
		
		if (isOutput) {
			$(connDiv).dblclick(function() {
				autoConnectOutputByExample(connDiv);
			});
		}
		
		return ep;
	}
	my.createJSPlumbEndpoint = createJSPlumbEndpoint;
	
	function rename(iodiv,data) {
		var n = $(iodiv).find(".ioname").text();

		var displayName = prompt("Display name for "+data.name+":",n);
		if (displayName != null) {
			if (displayName != "" && displayName != data.name) {
				data.displayName = displayName;
			}
			else {
				delete data.displayName;
				displayName = data.name;
			}

			$(iodiv).find(".ioname").html(displayName);
		}
		jsPlumb.recalculateOffsets(module.getDiv().attr('id'));
		jsPlumb.repaint(module.getDiv().attr('id'));
	}
	
	function handleContextMenuSelection(div,data,selection,event) {
		if (selection=="rename") {
			rename(div,data);
			event.stopPropagation();
		}
		else if (selection=="export") {
			toggleExport(div,data);
			event.stopPropagation();
		}
		else module.handleContextMenuSelection(div,data,selection,event);
	}
	my.handleContextMenuSelection = handleContextMenuSelection;
	
	// TODO: toJSON()
	
	return my;
}