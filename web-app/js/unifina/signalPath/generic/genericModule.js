SignalPath.GenericModule = function(data, canvas, prot) {
	prot = prot || {};
	
	var pub = SignalPath.EmptyModule(data, canvas, prot);

	prot.paramTable = null;
	prot.ioTable = null;

	prot.enableIONameChange = true;
	
	prot.inputsByName = {};
	prot.params = [];
	prot.inputs = [];
	prot.outputsByName = {};
	prot.outputs = [];
	
	// Updated on dragstart, used on drag event to repaint jsPlumb connectors
	var _cachedEndpoints = []

	function createModuleFooter() {
		// Button for toggling the clearState. Default true.
		
		var div = $("<div class='modulefooter'></div>");
		prot.div.append(div);
		
		var container = $("<div class='moduleSwitchContainer showOnFocus'></div>")
		div.append(container)
		
		if (prot.jsonData.canClearState==null || prot.jsonData.canClearState) {
			var clear = new SignalPath.IOSwitch(container, "moduleSwitch clearState", {
				getValue: (function(d){
					return function() { return d.clearState; };
				})(data),
				setValue: (function(d){
					return function(value) { return d.clearState = value; };
				})(data),
				buttonText: function() { return "CLEAR"; },
				tooltip: 'Clear module state at end of day'
			})
		}
		
		return div
	}
	prot.createModuleFooter = createModuleFooter;
	
	// Helper function for updateFrom and clone
	function collectRemoteEndpoints(endpoints,list) {
		$(endpoints).each(function(i,endpoint) {
			var name = endpoint.getName();
			var connectedEndpoints = endpoint.getConnectedEndpoints(); 
			if (connectedEndpoints.length>0) {
				$(connectedEndpoints).each(function(j,ce) {
					list.push({
						name: name,
						endpoint: ce
					});
				});
			}
		});
	}
	
	// PROTECTED FUNCTIONS
	
	var superUpdateFrom = pub.updateFrom;
	function updateFrom(data) {
		var oldInputConnections = [];
		var oldOutputConnections = [];
		
		collectRemoteEndpoints(prot.params, oldInputConnections);
		collectRemoteEndpoints(prot.inputs, oldInputConnections);
		collectRemoteEndpoints(prot.outputs, oldOutputConnections);

		// Reset the lookups
		prot.inputsByName = {};
		prot.params = [];
		prot.inputs = [];
		prot.outputsByName = {};
		prot.outputs = [];
		
		superUpdateFrom(data);
		
		// Reconnect io by name
		$(oldInputConnections).each(function(i,item) {
			// Find an input by that name
			var input = getInput(item.name);
			if (input!=null) {
				input.connect(item.endpoint);
			}
		});
		
		$(oldOutputConnections).each(function(i,item) {
			// Find an input by that name
			var output = getOutput(item.name);
			if (output!=null) {
				output.connect(item.endpoint);
			}
		});
		
		pub.redraw()
	}
	pub.updateFrom = updateFrom;
	
	var superCreateDiv = prot.createDiv;
	function createDiv() {
		superCreateDiv();
		
		// Module parameter table
		if (prot.jsonData.params && prot.jsonData.params.length > 0) {
			prot.paramTable = $("<table class='paramTable'></table>");
			prot.body.append(prot.paramTable);
			$.each(prot.jsonData.params, function(i,p) {
				prot.addParameter(p);
			});
		}

		// module inputs and outputs
		prot.ioTable = $("<table class='ioTable'></table>");
		prot.body.append(prot.ioTable);
		
		$(prot.jsonData.inputs).each(function(i,data) {
			prot.addInput(data);
		});

		$(prot.jsonData.outputs).each(function(i,data) {
			prot.addOutput(data);
		});

		prot.createModuleFooter()
		
		prot.div.on("dragstart", function(event, ui) {
			jsPlumb.recalculateOffsets(prot.div.attr('id'));
			_cachedEndpoints = prot.div.find(".endpoint")
		});
		
		// Update endpoint positions explicitly. You could replace this with jsPlumb drag handling, but weird things may happen!
		prot.div.on("drag", function(event, ui) {
			if (_cachedEndpoints.length)
				jsPlumb.repaint(_cachedEndpoints)
		});
		
		$(SignalPath).on("_signalPathLoadModulesReady", function() {
			prot.refreshConnections();
		});
		
		// A module is focused by default when it is created
		// Repeat the command here as focusing changes z-index of endpoints
		prot.addFocus(true);
	}
	prot.createDiv = createDiv;
	
	function disconnect() {
		$(prot.div).find("div.input").each(function(i,div) {
			jsPlumb.detachAllConnections(div);
		});
		$(prot.div).find("div.output").each(function(i,div) {
			jsPlumb.detachAllConnections(div);
		});
	}
	prot.disconnect = disconnect;
	
	var superClose = pub.close;
	function close() {
		disconnect();
		
		$(prot.div).find("div.input").each(function(i,div) {
			jsPlumb.removeAllEndpoints(div);
			// This call should not be necessary but there may be a bug in jsPlumb
			jsPlumb.dragManager.elementRemoved($(div).attr('id'));
		});
		$(prot.div).find("div.output").each(function(i,div) {
			jsPlumb.removeAllEndpoints(div);
			// This call should not be necessary but there may be a bug in jsPlumb
			jsPlumb.dragManager.elementRemoved($(div).attr('id'));
		});
		
		superClose();
	}
	pub.close = close;
	
	var super_addFocus = prot.addFocus;
	prot.addFocus = function(hold) {
		super_addFocus(hold);
		
		if (hold) {
			$.each(getParameters(), function(i, endpoint) {
				endpoint.jsPlumbEndpoint.addClass("holdFocus");
			});
			$.each(getInputs(), function(i, endpoint) {
				endpoint.jsPlumbEndpoint.addClass("holdFocus");
			});
			$.each(getOutputs(), function(i, endpoint) {
				endpoint.jsPlumbEndpoint.addClass("holdFocus");
			});
		}
	}
	
	/**
	 * Augment the module help text with endpoint-specific help tables
	 */
	var super_renderHelp = prot.renderHelp;
	prot.renderHelp = function(data, extended) {
		var result = super_renderHelp(data);
		
		// Add tip about clicking to show extended help
		if (!extended && (data.paramNames.length>0 || data.inputNames.length>0 || data.outputNames.length>0)) {
			result += "<p class='message'>Click the help button to show extended help.</p>";
		}
		else if (extended) {
		
			// Helper function to add io-specific help table
	    	var f = function(title,names,valMap) {
	    		var txt = "";
	    		if (names && names.length>0) {
	    			txt += "<h3>"+title+"</h3>";
		    		var $t = $("<table></table>");
		    		$(names).each(function(i,n) {
		    			$t.append("<tr><td>"+n+"</td><td>"+valMap[n]+"</td></tr>");
		    		});
		    		txt += $t[0].outerHTML;
	    		}
	    		return txt;
	    	}
	    	
	    	result += f("Parameters", data.paramNames, data.params);
	    	result += f("Inputs", data.inputNames, data.inputs);
	    	result += f("Outputs", data.outputNames, data.outputs);
    	
		}
    	return result;
	}
	
	var super_removeFocus = prot.removeFocus;
	prot.removeFocus = function() {
		super_removeFocus();
		
		$.each(getParameters(), function(i, endpoint) {
			endpoint.jsPlumbEndpoint.removeClass("holdFocus");
		});
		$.each(getInputs(), function(i, endpoint) {
			endpoint.jsPlumbEndpoint.removeClass("holdFocus");
		});
		$.each(getOutputs(), function(i, endpoint) {
			endpoint.jsPlumbEndpoint.removeClass("holdFocus");
		});
	}
	
	function getParameters() {
		return prot.params;
	}
	pub.getParameters = getParameters;
	
	function getInputs() {
		return prot.inputs;
	}
	pub.getInputs = getInputs;
	
	function getOutputs() {
		return prot.outputs;
	}
	pub.getOutputs = getOutputs;
	
	/**
	 * Returns an Input object
	 */
	function getInput(name) {
		return prot.inputsByName[name];
	}
	pub.getInput = getInput;
	
	/**
	 * Returns an Output object
	 */
	function getOutput(name) {
		return prot.outputsByName[name];
	}
	pub.getOutput = getOutput;
	
	function addParameter(data) {
		// Create room for the parameter in paramTable
		var row = $("<tr></tr>");
		prot.paramTable.append(row);
		
		var paramTd = $("<td></td>");
		row.append(paramTd);
		
		var endpoint = SignalPath.Parameter(data, paramTd, prot);
		endpoint.createDiv();
		prot.params.push(endpoint);
		prot.inputsByName[endpoint.getName()] = endpoint;
		
		return endpoint;
	}
	prot.addParameter = addParameter;
	
	function createRoomForIO(type) {
		// Find first empty input in the prot.ioTable
		var td = prot.ioTable.find("td."+type+":empty:first");
		// Add row if no suitable row found
		if (td==null || td.length == 0) {
			var newRow = $("<tr><td class='input'></td><td class='output'></td>");
			prot.ioTable.append(newRow);
			td = newRow.find("td."+type+":first");
		}
		return td;
	}
	
	prot.addInput = function(data, clazz) {
		clazz = clazz || SignalPath.Input
		
		var td = createRoomForIO("input");

		var endpoint = clazz(data, td, prot);
		endpoint.createDiv();
		prot.inputs.push(endpoint);
		prot.inputsByName[endpoint.getName()] = endpoint;
		
		return endpoint;
	}

	prot.addOutput = function(data, clazz) {
		clazz = clazz || SignalPath.Output
		
		var td = createRoomForIO("output");

		var endpoint = clazz(data, td, prot);
		endpoint.createDiv();
		prot.outputs.push(endpoint);
		prot.outputsByName[endpoint.getName()] = endpoint;
		
		return endpoint;
	}

	var superGetContextMenu = prot.getContextMenu;
	function getContextMenu(div) {
		var menu = superGetContextMenu();
		
		menu.push({title: "Disconnect all", cmd: "disconnect"});
		
		return menu;
	}
	prot.getContextMenu = getContextMenu;
	
	var superHandleContextMenuSelection = prot.handleContextMenuSelection;
	prot.handleContextMenuSelection = function(target, selection) {
		if (selection=="disconnect") {
			prot.disconnect();
		}
		else superHandleContextMenuSelection(target, selection);
	}
	
	var superPrepareCloneData = prot.prepareCloneData;
	prot.prepareCloneData = function(cloneData) {
		superPrepareCloneData(cloneData);
		
		$(cloneData.params).each(function(i,param) {
			param.id = null;
		});
		
		$(cloneData.inputs).each(function(i,input) {
			input.id = null;
		});
		
		// Outputs of cloned modules can never be connected, as every input can only take one connection!
		$(cloneData.outputs).each(function(i,output) {
			output.id = null;
			output.connected = false;
			delete output.targets;
		});
	}
	
	var super_clone = prot.clone;
	prot.clone = function() {
		var module = super_clone();
		module.refreshConnections();
	}
	
	function refreshConnections() {
		$(getParameters()).each(function(i,endpoint) {
			endpoint.refreshConnections();
		});
		$(getInputs()).each(function(i,endpoint) {
			endpoint.refreshConnections();
		});
		$(getOutputs()).each(function(i,endpoint) {
			endpoint.refreshConnections();
		});
	}
	pub.refreshConnections = refreshConnections;
	
	var super_receiveResponse = prot.receiveResponse
	prot.receiveResponse = function(payload) {
		super_receiveResponse(payload);
		if (payload.type=="paramChangeResponse") {
			// TODO handle param change response
		}
	}
	
	var super_redraw = pub.redraw
	pub.redraw = function() {
		super_redraw()
		
		jsPlumb.recalculateOffsets(prot.div.attr('id'));
		
		if ($(prot.div).find("div.input").length>0)
			jsPlumb.repaint($(prot.div).find("div.input"));
		if ($(prot.div).find("div.output").length>0)
			jsPlumb.repaint($(prot.div).find("div.output"));
	}
	
	var superToJSON = pub.toJSON;
	function toJSON() {
		prot.jsonData = superToJSON();

		// Get parameter JSON
		prot.jsonData.params = [];
		$(prot.params).each(function(i,endpoint) {
			prot.jsonData.params.push(endpoint.toJSON());
		});

		// Get input JSON
		prot.jsonData.inputs = [];
		$(prot.inputs).each(function(i,endpoint) {
			prot.jsonData.inputs.push(endpoint.toJSON());
		});
		
		// Get output JSON
		prot.jsonData.outputs = [];
		$(prot.outputs).each(function(i,endpoint) {
			prot.jsonData.outputs.push(endpoint.toJSON());
		});		

		return prot.jsonData;
	}
	pub.toJSON = toJSON;
	
	// Everything added to the public interface can be accessed from the
	// private interface too
	$.extend(prot,pub);
	
	return pub;
}
