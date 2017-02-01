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
	prot.moduleClosed = false
	
	// Updated on dragstart, used on drag event to repaint jsPlumb connectors
	var _cachedEndpoints = []

	function createModuleFooter() {
		var div = $("<div class='modulefooter'></div>")
		prot.div.append(div)
		prot.footer = div

		var container = $("<div class='moduleSwitchContainer showOnFocus'></div>")
		div.append(container)

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
		
		prot.div.on("dragStart", function(event, ui) {
			jsPlumb.recalculateOffsets(prot.div.attr('id'));
			_cachedEndpoints = prot.div.find(".endpoint")
		});
		
		// Update endpoint positions explicitly. You could replace this with jsPlumb drag handling, but weird things may happen!
		prot.div.on("dragMove", function(event, ui) {
			if (_cachedEndpoints.length)
				jsPlumb.repaint(_cachedEndpoints)
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
		prot.moduleClosed = true
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
			$.each(getEndpoints(), function(i, endpoint) {
				if (endpoint.jsPlumbEndpoint) {
					endpoint.jsPlumbEndpoint.addClass("holdFocus");
				}
			})
		}
	}
	
	/**
	 * Augment the module help text with endpoint-specific help tables
	 */
	var super_renderHelp = prot.renderHelp;
	prot.renderHelp = function(data, extended) {
		var result = super_renderHelp(data, extended);
		
		// Add tip about clicking to show extended help
		if (!extended && (data.paramNames && data.paramNames.length>0 || data.inputNames && data.inputNames.length>0 || data.outputNames && data.outputNames.length>0)) {
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

		$.each(getEndpoints(), function(i, endpoint) {
			if (endpoint.jsPlumbEndpoint) {
				endpoint.jsPlumbEndpoint.removeClass("holdFocus");
			}
		})
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

	function getEndpoints() {
		return getParameters().concat(getInputs()).concat(getOutputs())
	}
	pub.getEndpoints = getEndpoints

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

	function findInputByDisplayName(name) {
		var found = null
		$(prot.inputs).each(function(i, endpoint) {
			if (endpoint.json.displayName === name || endpoint.json.name === name) {
				found = endpoint
				return
			}
		});
		return found
	}
	pub.findInputByDisplayName = findInputByDisplayName

	function findOutputByDisplayName(name) {
		var found = null
		$(prot.outputs).each(function(i, endpoint) {
			if (endpoint.json.displayName === name || endpoint.json.name === name) {
				found = endpoint
				return
			}
		});
		return found
	}
	pub.findOutputByDisplayName = findOutputByDisplayName
	
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

	prot.addPlaceholderOutput = function() {
		var td = createRoomForIO("output");
		td.append("<div class='endpoint placeholder'></div>")
	}
	
	prot.addInput = function(data, clazz) {
		clazz = clazz || data.jsClass || SignalPath.Input
		if (typeof clazz === "string") {
			clazz = eval("SignalPath." + clazz);
		}
		
 		var td = createRoomForIO("input");

		var endpoint = clazz(data, td, prot);
		endpoint.createDiv();
		prot.inputs.push(endpoint);
		prot.inputsByName[endpoint.getName()] = endpoint;
		
		return endpoint;
	}

	prot.addOutput = function(data, clazz) {
		clazz = clazz || data.jsClass || SignalPath.Output
		if (typeof clazz === "string") {
			clazz = eval("SignalPath." + clazz);
		}
		
		var td = createRoomForIO("output");

		var endpoint = clazz(data, td, prot);
		endpoint.createDiv();
		prot.outputs.push(endpoint);
		prot.outputsByName[endpoint.getName()] = endpoint;
		
		return endpoint;
	}
	
	prot.removeInput = function(name) {
		var _this = this
		// Remove the endpoint async to ensure it is run after the spDisconnect event has been handled completely
		setTimeout(function() {
			if (!prot.moduleClosed) {
				var el = prot.inputsByName[name]
				if (el) {
					var index = prot.inputs.indexOf(el)
					if (index <= -1) {
						throw "Shouldn't be here!"
					}
					prot.inputs.splice(index, 1)
					delete prot.inputsByName[name]

					try {
						jsPlumb.remove(el.div)
					} finally {
						el.parentDiv.empty()
						_this.redraw()
						if (el.parentDiv.parent().find("td.output:empty").length !== 0) {
							el.parentDiv.parent().remove()
						}
					}
				}
			}
		}, 0)
	}

	prot.removeOutput = function(name) {
		var _this = this
		// Remove the endpoint async to ensure it is run after the spDisconnect event has been handled completely
		setTimeout(function() {
			if (!prot.moduleClosed) {
				var el = prot.outputsByName[name]
				if (el) {
					var index = prot.outputs.indexOf(el)
					if (index <= -1) {
						throw "Shouldn't be here!"
					}
					prot.outputs.splice(index, 1)
					delete prot.outputsByName[name]

					try {
						jsPlumb.remove(el.div)
					} finally {
						el.parentDiv.empty()
						_this.redraw()
						if (el.parentDiv.parent().find("td.input:empty").length !== 0) {
							el.parentDiv.parent().remove()
						}
					}
				}
			}
		}, 0)
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
		super_clone(function(module) {
			module.refreshConnections();
		});
	}
	
	function refreshConnections() {
		$(getEndpoints()).each(function(i,endpoint) {
			endpoint.refreshConnections();
		})
	}
	pub.refreshConnections = refreshConnections;
	
	var super_redraw = pub.redraw
	pub.redraw = function() {
		super_redraw()
		
		jsPlumb.recalculateOffsets(prot.div.attr('id'));
		
        var inputs = prot.div.find("div.input")
		if (inputs.length > 0)
			jsPlumb.repaint(inputs);
        var outputs = prot.div.find("div.output")
		if (outputs.length > 0)
			jsPlumb.repaint(outputs);
	}
	
	var superToJSON = pub.toJSON;
	function toJSON() {
		prot.jsonData = superToJSON();

		// Get parameter JSON
		prot.jsonData.params = [];
		$(prot.params).each(function(i, endpoint) {
			prot.jsonData.params.push(endpoint.toJSON());
		});

		// Get input JSON
		prot.jsonData.inputs = [];
		$(prot.inputs).each(function(i, endpoint) {
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
	$.extend(prot, pub);

	$(SignalPath).on("_signalPathLoadModulesReady", prot.refreshConnections);
	$(prot).on('closed', function() {
		$(SignalPath).off("_signalPathLoadModulesReady", prot.refreshConnections);
	})

	return pub;
}
