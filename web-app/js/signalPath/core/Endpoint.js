SignalPath.Endpoint = function(json, parentDiv, module, type, pub) {
	pub = pub || {};
	
	pub.json = json;
	pub.parentDiv = parentDiv;
	pub.module = module;
	pub.type = type;
	
	function createDiv() {
		
		// Create connection div
		var div = $("<div class='endpoint "+pub.type+" "+pub.json.type+"'></div>");
		div.data("spObject",pub);
		pub.div = div;
		
		// Create id if not yet created
		if (!pub.json.id || pub.json.id.length<20) {
			var oldId = pub.json.id;
			pub.json.id = generateId();
			
			if (oldId)
				SignalPath.replacedIds[oldId] = pub.json.id;
		}
		
		div.attr("id",pub.json.id);
		
		// Name holder
		var ioname = $("<div class='ioname'>"+pub.getDisplayName()+"</span>");
		div.append(ioname);
		
		// Add to parent
		pub.parentDiv.append(div);

		if (pub.json["export"])
			setExport(div,pub.json,true);
		
		// Don't create jsPlumb endpoint if pub.json.canConnect is false (default: true)
		if (pub.json.canConnect==null || pub.json.canConnect) {
			// TODO: endpointId is for backwards compatibility
			var id = (pub.json.endpointId != null ? pub.json.endpointId : pub.json.id);
			pub.jsPlumbEndpoint = createJSPlumbEndpoint(pub.json, div, id);
		}
		
		// Create io settings
		pub.createSettings(div,pub.json);
		
		// Bind context menu listeners
		if (!pub.disableContextMenu) {
			div.addClass("context-menu");
		}
		
		div.on('spContextMenuSelection', (function(d,j) {
			return function(event,selection) {
				pub.handleContextMenuSelection(d,j,selection,event);
			};
		})(div,pub.json));
		
		div.trigger("spIOReady");
		
		return div;
	}
	pub.createDiv = createDiv;
	
	function setExport(iodiv,data,value) {
		if (value) {
			iodiv.addClass("export");
			data["export"] = true;
		}
		else {
			iodiv.removeClass("export");
			data["export"] = false;
		}
	}
	pub.setExport = setExport;
	
	function toggleExport(iodiv,data) {
		pub.setExport(iodiv,data,!data["export"]);
	}
	pub.toggleExport = toggleExport;
	
	function generateId() {
		var id = "myId_"+pub.module.hash+"_"+new Date().getTime();
		
		// Check for clashes
		while ($("#"+id).length>0)
			id += "_";
				
		return id;
	}	

	function getId() {
		return pub.json.id;
	}
	pub.getId = getId;
	
	function getJSPlumbEndpointOptions(json,connDiv) {
		return {
			isSource:true, 
			isTarget:true,
			dragOptions: {},
			dropOptions: {},
			beforeDrop: function(info) {
				return pub.checkConnection(info) && pub.checkConnectionDirection(info);
			},
			connectorOverlays: [["Arrow", {direction:1, paintStyle: {cssClass:"arrow"}}]],
		};
	}
	pub.getJSPlumbEndpointOptions = getJSPlumbEndpointOptions;
	
	function createJSPlumbEndpoint(json,connDiv,id) {
		var isOutput = connDiv.hasClass('output');
		var isInput = connDiv.hasClass('input');
		
		// For connections
		// TODO: define overlay in theme?
		var overlays = [["Arrow", {direction:(isInput ? -1 : 1), paintStyle: {cssClass:"arrow"}}]];
		
		var ep = jsPlumb.addEndpoint(connDiv, pub.getJSPlumbEndpointOptions(json.connDiv));
		
		$(connDiv).data("acceptedTypes", (json.acceptedTypes!=null ? json.acceptedTypes : [json.type]));
		
		return ep;
	}
	pub.createJSPlumbEndpoint = createJSPlumbEndpoint;
	
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
		var menu = pub.module.getContextMenu(div);
		
//		if (div.hasClass("ioname")) {
		menu.push({title: "Rename", cmd: "rename"});
		menu.push({title: "Toggle export", cmd: "export"});
//		}
		return menu;
	}
	pub.getContextMenu = getContextMenu;
	
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
	pub.handleContextMenuSelection = handleContextMenuSelection;
	
	function getName() {
		return pub.json.name;
	}
	pub.getName = getName;

	function getDisplayName() {
		return (pub.json.displayName ? pub.json.displayName : pub.json.name);
	}
	pub.getDisplayName = getDisplayName;
	
	function checkConnection(connection) {
		// Endpoints must contain at least one acceptedType in common
		// "Object" types can be connected to anything
		var arr1 = $("#"+connection.sourceId).data("acceptedTypes");
		var arr2 = $("#"+connection.targetId).data("acceptedTypes");
		
		for(var i = 0; i<arr1.length; i++)
		    for(var j=0; j<arr2.length; j++)
		        if(arr1[i]==arr2[j] || arr1[i]=="Object" || arr2[j]=="Object")
		            return true;

		SignalPath.options.errorHandler({msg:"These endpoints can not be connected! Accepted types at source: "+arr1+". Accepted types at target: "+arr2+"."});
		return false;
	}
	pub.checkConnection = checkConnection;
	
	function getAcceptedTypes() {
		return json.acceptedTypes;
	}
	pub.getAcceptedTypes = getAcceptedTypes;
	
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
	pub.checkConnectionDirection = checkConnectionDirection;
	
	pub.createSettings = function(div,data) {

	}
	
	// abstract, must be overridden	
	function connect(endpoint) {
		throw new Exception("connect() called in Endpoint");
	}
	pub.connect = connect;
	
	// abstract, must be overridden
	function getConnectedEndpoints() {
		throw new Exception("getConnectedEndpoints() called in Endpoint");
	}
	pub.getConnectedEndpoints = getConnectedEndpoints;

	// abstract, must be overridden
	function refreshConnections() {
		throw new Exception("refreshConnections() called in Endpoint");
	}
	
	// abstract, must be overridden
	function toJSON() {
		throw new Exception("toJSON() called in Endpoint");
	}
	pub.toJSON = toJSON;
	
	return pub;
}