SignalPath.Endpoint = function(json, parentDiv, module, type, my) {
	my = my || {};
	
	my.json = json;
	my.parentDiv = parentDiv;
	my.module = module;
	my.type = type;
	
	function createDiv() {
		
		// Create connection div
		var div = $("<div class='"+my.type+" "+my.json.type+"'></div>");
		div.data("spObject",my);
		my.div = div;
		
		// Create id if not yet created
		if (!my.json.id || my.json.id.length<20) {
			var oldId = my.json.id;
			my.json.id = generateId();
			
			if (oldId)
				SignalPath.replacedIds[oldId] = my.json.id;
		}
		
		div.attr("id",my.json.id);
		
		// Name holder
		var ioname = $("<div class='ioname'>"+(my.json.displayName ? my.json.displayName : my.json.name)+"</span>");
		div.append(ioname);
		
		// Add to parent
		my.parentDiv.append(div);

		if (my.json.export)
			setExport(div,my.json,true);
		
		// Don't create jsPlumb endpoint if my.json.canConnect is false (default: true)
		if (my.json.canConnect==null || my.json.canConnect) {
			// TODO: endpointId is for backwards compatibility
			var id = (my.json.endpointId != null ? my.json.endpointId : my.json.id);
			my.jsPlumbEndpoint = createJSPlumbEndpoint(my.json, div, id);
		}
		
		// Create io settings
		my.createSettings(div,my.json);
		
		// Bind context menu listeners
		if (!my.disableContextMenu) {
			div.addClass("context-menu");
		}
		
		div.on('spContextMenuSelection', (function(d,j) {
			return function(event,selection) {
				my.handleContextMenuSelection(d,j,selection,event);
			};
		})(div,my.json));
		
		div.trigger("spIOReady");
		
		return div;
	}
	my.createDiv = createDiv;
	
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
		var id = "myId_"+my.module.hash+"_"+new Date().getTime();
		
		// Check for clashes
		while ($("#"+id).length>0)
			id += "_";
				
		return id;
	}	

	function getId() {
		return my.json.id;
	}
	my.getId = getId;
	
	function getJSPlumbEndpointOptions(json,connDiv) {
		return {
			isSource:true, 
			isTarget:true,
			dragOptions: {},
			dropOptions: {},
			beforeDrop: function(info) {
				return my.checkConnection(info) && my.checkConnectionDirection(info);
			},
			connectorOverlays: [["Arrow", {direction:1, paintStyle: {cssClass:"arrow"}}]],
		};
	}
	my.getJSPlumbEndpointOptions = getJSPlumbEndpointOptions;
	
	function createJSPlumbEndpoint(json,connDiv,id) {
		var isOutput = connDiv.hasClass('output');
		var isInput = connDiv.hasClass('input');
		
		// For connections
		// TODO: define overlay in theme?
		var overlays = [["Arrow", {direction:(isInput ? -1 : 1), paintStyle: {cssClass:"arrow"}}]];
		
		var ep = jsPlumb.addEndpoint(connDiv, my.getJSPlumbEndpointOptions(json.connDiv));
		ep.bind("click", function(endpoint) {
			console.log(endpoint);
		});
		
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
		jsPlumb.recalculateOffsets(module.div.attr('id'));
		jsPlumb.repaint(module.div.attr('id'));
	}
	
	function getContextMenu(div) {
		var menu = my.module.getContextMenu(div);
		
//		if (div.hasClass("ioname")) {
		menu.push({title: "Rename I/O", cmd: "rename"});
		menu.push({title: "Toggle export", cmd: "export"});
//		}
		return menu;
	}
	my.getContextMenu = getContextMenu;
	
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
	
	function getName() {
		return my.json.name;
	}
	my.getName = getName;

	
	function checkConnection(connection) {
		// Endpoints must contain at least one acceptedType in common
		var arr1 = $("#"+connection.sourceId).data("acceptedTypes");
		var arr2 = $("#"+connection.targetId).data("acceptedTypes");
		
		for(var i = 0; i<arr1.length; i++)
		    for(var j=0; j<arr2.length; j++)
		        if(arr1[i]==arr2[j])
		            return true;

		SignalPath.options.errorHandler("These endpoints can not be connected! Accepted types at source: "+arr1+". Accepted types at target: "+arr2+".");
		return false;
	}
	my.checkConnection = checkConnection;
	
	function checkConnectionDirection(info) {
		// Check that the source of this connection is an input and the target is an output.
		// If they are the other way around, create a reversed connection and return false for this connection.
		
		var source = $("#"+info.sourceId);
		var target = $("#"+info.targetId);
		
		if (source.hasClass("input") && target.hasClass("output"))
			return true;
		else if (source.hasClass("output") && target.hasClass("input")) {
			// Reverse the connection
			jsPlumb.connect({source:target.data("spObject").jsPlumbEndpoint, target:source.data("spObject").jsPlumbEndpoint});
			return false;
		}
		// Don't allow input-input or output-output connections
		else return false;
	}
	my.checkConnectionDirection = checkConnectionDirection;
	
	my.createSettings = function(div,data) {

	}
	
	// abstract, must be overridden	
	function connect(endpoint) {
		throw new Exception("connect() called in Endpoint");
	}
	my.connect = connect;
	
	// abstract, must be overridden
	function getConnectedEndpoints() {
		throw new Exception("getConnectedEndpoints() called in Endpoint");
	}
	my.getConnectedEndpoints = getConnectedEndpoints;

	// abstract, must be overridden
	function refreshConnections() {
		throw new Exception("refreshConnections() called in Endpoint");
	}
	
	// abstract, must be overridden
	function toJSON() {
		throw new Exception("toJSON() called in Endpoint");
	}
	my.toJSON = toJSON;
	
	return my;
}