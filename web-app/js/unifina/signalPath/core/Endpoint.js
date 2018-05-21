SignalPath.Endpoint = function(json, parentDiv, module, type, pub) {
	pub = pub || {};

	pub.json = json;
	pub.parentDiv = parentDiv;
	pub.module = module;
	pub.type = type;
	pub.iostate = null;

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
		var ioname = $("<div class='ioname'></div>");
		ioname.append(pub.getDisplayName())
		div.append(ioname);

		// State holder
		pub.iostate = $("<div class='iostate'></div>")
		div.append(pub.iostate)

		// Add to parent
		pub.parentDiv.append(div);

		if (pub.json["export"])
			setExport(div,pub.json,true);

		// Don't create jsPlumb endpoint if pub.json.canConnect is false (default: true)
		if (pub.json.canConnect==null || pub.json.canConnect) {
			// TODO: endpointId is for backwards compatibility
			var id = (pub.json.endpointId != null ? pub.json.endpointId : pub.json.id);
			pub.jsPlumbEndpoint = createJSPlumbEndpoint(pub.json, div, id);

			if (pub.json.requiresConnection)
				pub.addClass("warning")
		}

		// Create io settings
		pub.createSettings(div,pub.json);

		// Bind context menu listeners
		if (!pub.disableContextMenu) {
			div.addClass("context-menu");

			// When left-clicking on ioname, simulate right click
			// This is the current way of enabling context menu on touch devices
			ioname.click(function(e) {
				e.type = "contextmenu"
				$(ioname).trigger(e)
			})
		}

		// Bind basic connection event handlers
		div.bind("spConnect", (function(me) {
			return function(event, output) {
				me.json.connected = me.isConnected()
				if (me.json.connected) {
					me.div.addClass("connected");
				}

				if (me.hasWarning())
					me.addClass("warning")
				else me.removeClass("warning")
			}
		})(pub));

		div.bind("spDisconnect", (function(me) {
			return function(event, output) {
				me.json.connected = me.isConnected()
				if (!me.json.connected) {
					me.div.removeClass("connected");
				}

				if (me.hasWarning())
					me.addClass("warning")
				else me.removeClass("warning")
			}
		})(pub));

		div.trigger("spIOReady");

		return div;
	}
	pub.createDiv = createDiv;

	pub.hasWarning = function() {
		return pub.json.requiresConnection && !pub.isConnected()
	}

	function addClass(cls) {
		pub.div.addClass(cls)
		pub.jsPlumbEndpoint.addClass(cls)
	}
	pub.addClass = addClass

	function removeClass(cls) {
		pub.div.removeClass(cls)
		pub.jsPlumbEndpoint.removeClass(cls)
	}
	pub.removeClass = removeClass

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
			anchors: ['RightMiddle'],
			beforeDrop: function(info) {
				return pub.checkConnection(info) && pub.checkConnectionDirection(info);
			},
			connectorOverlays: [["Arrow", {direction:1, paintStyle: {cssClass:"arrow"}}]],
			cssClass: (pub.json.requiresConnection ? "requiresConnection" : "")
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

		$(connDiv).data("acceptedTypes", getAcceptedTypes());

		// Add reference to this object to the jsPlumb Endpoint canvas element
		$(ep.canvas).data("spObject", pub);

		return ep;
	}
	pub.createJSPlumbEndpoint = createJSPlumbEndpoint;

	function rename(iodiv,data) {
		var defaultValue = $(iodiv).find(".ioname").text();

		bootbox.prompt({
			title: "Display name for "+data.name,
			value: defaultValue,
			className: 'rename-endpoint-dialog',
			callback: function(displayName) {
				if (displayName != null) {
					pub.setDisplayName(displayName)
                    module.redraw()
				}
			}
		});
	}

	function setDisplayName(displayName) {
		var data = pub.json;
        if (displayName != "" && displayName != data.name) {
            data.displayName = displayName;
        } else {
            delete data.displayName;
            displayName = data.name;
        }
        $(pub.div).find(".ioname").text(displayName);
	}
	pub.setDisplayName = setDisplayName

    function getContextMenu(div) {
		var menu = pub.module.getContextMenu(div);

		menu.push({title: "Rename", cmd: "rename"});
		menu.push({title: "Toggle export", cmd: "export"});

		return menu;
	}
	pub.getContextMenu = getContextMenu;

	function handleContextMenuSelection(target, selection) {
		if (selection=="rename") {
			rename(pub.div,pub.json);
		}
		else if (selection=="export") {
			toggleExport(pub.div,pub.json);
		}
		else module.handleContextMenuSelection(target, selection);
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

	function updateState(stateString) {
		pub.iostate.text(stateString)
	}
	pub.updateState = updateState;

	function checkTypeArrays(arr1, arr2) {
		for(var i = 0; i<arr1.length; i++)
		    for(var j=0; j<arr2.length; j++)
		        if(arr1[i]==arr2[j] || arr1[i]=="Object" || arr2[j]=="Object")
		            return true
		return false
	}

	function checkConnection(connection) {
		// Endpoints must contain at least one acceptedType in common
		// "Object" types can be connected to anything
		var arr1 = $("#"+connection.sourceId).data("acceptedTypes");
		var arr2 = $("#"+connection.targetId).data("acceptedTypes");

		if (checkTypeArrays(arr1,arr2)) {
			return true
		}
		else {
			SignalPath.options.errorHandler({msg:"These endpoints can not be connected! Accepted types at source: "+arr1+". Accepted types at target: "+arr2+"."});
			return false;
		}
	}
	pub.checkConnection = checkConnection;

	function acceptsTypes(other) {
		return checkTypeArrays(getAcceptedTypes(), other)
	}

	function getAcceptedTypes() {
		return (json.acceptedTypes!=null ? json.acceptedTypes : [json.type])
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

	function isConnected() {
		return pub.jsPlumbEndpoint && pub.jsPlumbEndpoint.connections.length > 0
	}
	pub.isConnected = isConnected

	pub.toJSON = function() {
		return pub.json;
	}

	// abstract, must be overridden
	function connect(endpoint) {
		throw new Exception("connect() called in Endpoint");
	}
	pub.connect = connect;

	// abstract, must be overridden
	function disconnect() {
		throw new Exception("disconnect() called in Endpoint");
	}
	pub.disconnect = disconnect;

	// abstract, must be overridden
	function getConnectedEndpoints() {
		throw new Exception("getConnectedEndpoints() called in Endpoint");
	}
	pub.getConnectedEndpoints = getConnectedEndpoints;

	// abstract, must be overridden
	function refreshConnections() {
		throw new Exception("refreshConnections() called in Endpoint");
	}

	return pub;
};

(function() {
	var jsPlumbReset = jsPlumb.reset

	jsPlumb.reset = function() {
		jsPlumbReset.apply(jsPlumb)

		// Bind connection and disconnection events
		jsPlumb.bind("connection", function(connection) {
			$(connection.source).trigger("spConnect", $(connection.target).data("spObject"));
			$(connection.target).trigger("spConnect", $(connection.source).data("spObject"));
		});
		jsPlumb.bind("connectionDetached", function(connection) {
			if (!connection.connection.pending) {
				$(connection.source).trigger("spDisconnect", $(connection.target).data("spObject"));
				$(connection.target).trigger("spDisconnect", $(connection.source).data("spObject"));
			}
		});
		// "connection" event is also fired on connection move, so need to report just the disconnect part
		jsPlumb.bind("connectionMoved", function(info, originalEvent) {
			// info.index: 0 if source was moved, 1 if target was moved
			var originalElement = (info.index ? info.originalTargetEndpoint.element : info.originalSourceEndpoint.element)
			var theOtherElement = (info.index ? info.originalSourceEndpoint.element : info.originalTargetEndpoint.element)

			$(originalElement).trigger("spDisconnect", $(theOtherElement).data("spObject"));
			$(theOtherElement).trigger("spDisconnect", $(originalElement).data("spObject"));
		});

		jsPlumb.bind("connectionDrag", function (conn) {
			// Highlight valid targets
			var $epDiv = $("#" + conn.sourceId)
			var ep = $epDiv.data("spObject")
			var inputOrOutput = ($epDiv.hasClass("input") && conn.pending || $epDiv.hasClass("output") && !conn.pending ? "output" : "input")
			var acceptedTypes = ep.getAcceptedTypes()
			$(acceptedTypes).each(function(i, type) {
				var $validTargets = (type === "Object" ? $(".endpoint." + inputOrOutput) : $(".endpoint." + inputOrOutput + "." + type + "," + ".endpoint." + inputOrOutput + ".Object"))

				$validTargets.each(function(j, target) {
					var spObject = $(target).data("spObject")
					if (!spObject.jsPlumbEndpoint.isFull())
						spObject.addClass("highlight")
				})
			})
		})

		jsPlumb.bind("connectionDragStop", function(conn) {
			// Un-highlight
			$(".endpoint.highlight").each(function(i, target) {
				$(target).data("spObject").removeClass("highlight")
			})
		})
	}
})()
