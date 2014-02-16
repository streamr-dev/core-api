SignalPath.GenericModule = function(data, canvas, my) {
	my = my || {};
	
	var that = SignalPath.EmptyModule(data, canvas, my);

	my.paramTable = null;
	my.ioTable = null;

	my.enableIONameChange = true;
	
	my.inputsByName = {};
	my.params = [];
	my.inputs = [];
	my.outputsByName = {};
	my.outputs = [];
	
	// TODO: this must be updated to work someday
//	function autoConnectInputs() {
//		var outputs = $("div.output");
//		var oIdx = 0;
//		$(my.div).find("div.input").each(function(i,input) {
//			// Skip parameters
//			if (!$(input).hasClass("parameter")) {
//				// Is this input connected?
//				var connections = jsPlumb.getConnections({source:input.attr("id"), scope:"*"});
//				if (connections.length==0) {
//					// Find an unconnected output
//					while (oIdx<outputs.length) {
//						var output = outputs[oIdx];
//						var connections = jsPlumb.getConnections({target:output.attr("id"), scope:"*"});
//						if (connections.length==0) {
//							jsPlumb.connect({source:input, target:output});
//							break;
//						}
//						oIdx++;
//					}
//				}
//			}
//		});
//	}

	function createModuleFooter() {
		// Button for toggling the clearState. Default true.
		
		var div = $("<div class='modulefooter'></div>");
		my.div.append(div);
		
		if (my.jsonData.canClearState==null || my.jsonData.canClearState) {
			var clear = $("<div class='moduleSwitch clearState'>CLEAR</div>");
			if (my.jsonData.clearState==null)
				my.jsonData.clearState = true;

			clear.addClass(my.jsonData.clearState ? "ioSwitchTrue" : "ioSwitchFalse");
			div.append(clear);
			clear.click(function() {
				if (my.jsonData.clearState) {
					my.jsonData.clearState = false;
					clear.removeClass("ioSwitchTrue");
					clear.addClass("ioSwitchFalse");
				}
				else {
					my.jsonData.clearState = true;
					clear.addClass("ioSwitchTrue");
					clear.removeClass("ioSwitchFalse");
				}
			});
		}
	}
	my.createModuleFooter = createModuleFooter;
	
	// TODO: this must be updated to work someday
//	function autoConnectOutputByExample(output) {
//		var connections = jsPlumb.getConnections({target:output.attr("id")});
//		
//		if (connections.length>0) {
//			var targets = [];
//			
//			$(connections).each(function(j,connection) {
//				var moduleName = connection.endpoints[0].element.parents(".component").find(".modulename").text();
//				var inputName = connection.endpoints[0].element.children(".ioname").text();
//				targets.push({
//					output: output,
//					moduleName: moduleName,
//					inputName: inputName
//				});
//			});
//			
//			$(targets).each(function(j,target) {
//				// Get all module names
//				var modules = $(".component");
//				$(modules).each(function(i,module) {
//					var moduleName = $(module).find(".modulename").text();
//					// If the module name is a match, look for an input with the correct name
//					if (moduleName==target.moduleName) {
//						var inputs = $(module).find("div.input");
//						$(inputs).each(function(k,input) {
//							var inputName = $(input).find(".ioname").text();
//							var connections = jsPlumb.getConnections({source:input.attr("id")});
//							if (inputName==target.inputName && connections.length==0)
//								jsPlumb.connect({source:input, target:target.output});
//						});
//					}
//				});
//			});
//		}
//	}

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
	
	var superUpdateFrom = that.updateFrom;
	function updateFrom(data) {
		var oldInputConnections = [];
		var oldOutputConnections = [];
		
		collectRemoteEndpoints(my.params, oldInputConnections);
		collectRemoteEndpoints(my.inputs, oldInputConnections);
		collectRemoteEndpoints(my.outputs, oldOutputConnections);

		// Reset the lookups
		my.inputsByName = {};
		my.params = [];
		my.inputs = [];
		my.outputsByName = {};
		my.outputs = [];
		
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
		
		if ($(my.div).find("div.input").length>0)
			jsPlumb.repaint($(my.div).find("div.input"));
		if ($(my.div).find("div.output").length>0)
			jsPlumb.repaint($(my.div).find("div.output"));
	}
	that.updateFrom = updateFrom;
	
	var superCreateDiv = my.createDiv;
	function createDiv() {
		superCreateDiv();
		
		// Destroy the jQuery draggable and replace it via jsPlumb.
		// This circumvents a bug in jsPlumb that causes the endpoints to not be repositioned after dragging.
		$(my.div).draggable("destroy");
		jsPlumb.draggable($(my.div).attr("id"), my.dragOptions);
		
		// Module parameter table
		if (my.jsonData.params && my.jsonData.params.length > 0) {
			my.paramTable = $("<table class='paramTable'></table>");
			my.body.append(my.paramTable);
			$.each(my.jsonData.params, function(i,p) {
				my.addParameter(p);
			});
		}

		// module inputs and outputs
		my.ioTable = $("<table class='ioTable'></table>");
		my.body.append(my.ioTable);
		
		$(my.jsonData.inputs).each(function(i,data) {
			my.addInput(data);
		});

		$(my.jsonData.outputs).each(function(i,data) {
			my.addOutput(data);
		});

		createModuleFooter();
		
		my.div.on( "dragstart", function(event, ui) {
			jsPlumb.recalculateOffsets(my.div.attr('id'));
		});
		
		$(SignalPath).on("_signalPathLoadModulesReady", function() {
			my.refreshConnections();
		});
		
		// TODO: re-enable, function must be brought up to date
		//my.title.dblclick(autoConnectInputs);
	}
	my.createDiv = createDiv;
	
	function disconnect() {
		$(my.div).find("div.input").each(function(i,div) {
			jsPlumb.detachAllConnections(div);
		});
		$(my.div).find("div.output").each(function(i,div) {
			jsPlumb.detachAllConnections(div);
		});
	}
	my.disconnect = disconnect;
	
	var superClose = that.close;
	function close() {
		disconnect();
		
		$(my.div).find("div.input").each(function(i,div) {
			jsPlumb.removeAllEndpoints(div);
			// This call should not be necessary but there may be a bug in jsPlumb
			jsPlumb.dragManager.elementRemoved($(div).attr('id'));
		});
		$(my.div).find("div.output").each(function(i,div) {
			jsPlumb.removeAllEndpoints(div);
			// This call should not be necessary but there may be a bug in jsPlumb
			jsPlumb.dragManager.elementRemoved($(div).attr('id'));
		});
		
		superClose();
	}
	that.close = close;
	
	function getParameters() {
		return my.params;
	}
	that.getParameters = getParameters;
	
	function getInputs() {
		return my.inputs;
	}
	that.getInputs = getInputs;
	
	function getOutputs() {
		return my.outputs;
	}
	that.getOutputs = getOutputs;
	
	/**
	 * Returns an Input object
	 */
	function getInput(name) {
		return inputsByName[name];
	}
	that.getInput = getInput;
	
	/**
	 * Returns an Output object
	 */
	function getOutput(name) {
		return outputsByName[name];
	}
	that.getOutput = getOutput;
	
	function addParameter(data) {
		// Create room for the parameter in paramTable
		var row = $("<tr></tr>");
		my.paramTable.append(row);
		
		var paramTd = $("<td></td>");
		row.append(paramTd);
		
		var endpoint = SignalPath.Parameter(data, paramTd, my);
		endpoint.createDiv();
		my.params.push(endpoint);
		my.inputsByName[endpoint.getName()] = endpoint;
		
		return endpoint;
	}
	my.addParameter = addParameter;
	
	function createRoomForIO(type) {
		// Find first empty input in the my.ioTable
		var td = my.ioTable.find("td."+type+":empty:first");
		// Add row if no suitable row found
		if (td==null || td.length == 0) {
			var newRow = $("<tr><td class='input'></td><td class='output'></td>");
			my.ioTable.append(newRow);
			td = newRow.find("td."+type+":first");
		}
		return td;
	}
	
	my.addInput = function(data) {
		var td = createRoomForIO("input");

		var endpoint = SignalPath.Input(data, td, my);
		endpoint.createDiv();
		my.inputs.push(endpoint);
		my.inputsByName[endpoint.getName()] = endpoint;
		
		return endpoint;
	}

	my.addOutput = function(data) {
		var td = createRoomForIO("output");

		var endpoint = SignalPath.Output(data, td, my);
		endpoint.createDiv();
		my.outputs.push(endpoint);
		my.outputsByName[endpoint.getName()] = endpoint;
		
		return endpoint;
	}

	var superGetContextMenu = my.getContextMenu;
	function getContextMenu(div) {
		var menu = superGetContextMenu();
		
		menu.push({title: "Disconnect all", cmd: "disconnect"});
		
		return menu;
	}
	my.getContextMenu = getContextMenu;
	
	var superHandleContextMenuSelection = my.handleContextMenuSelection;
	my.handleContextMenuSelection = function(div,data,selection,event) {
		if (selection=="disconnect") {
			my.disconnect();
			event.stopPropagation();
		}
		else superHandleContextMenuSelection(div,data,selection,event);
	}
	
	var superPrepareCloneData = my.prepareCloneData;
	my.prepareCloneData = function(cloneData) {
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
	
	var super_clone = my.clone;
	my.clone = function() {
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
	that.refreshConnections = refreshConnections;
	
	var superToJSON = that.toJSON;
	function toJSON() {
		my.jsonData = superToJSON();

		// Get parameter JSON
		my.jsonData.params = [];
		$(my.params).each(function(i,endpoint) {
			my.jsonData.params.push(endpoint.toJSON());
		});

		// Get input JSON
		my.jsonData.inputs = [];
		$(my.inputs).each(function(i,endpoint) {
			my.jsonData.inputs.push(endpoint.toJSON());
		});
		
		// Get output JSON
		my.jsonData.outputs = [];
		$(my.outputs).each(function(i,endpoint) {
			my.jsonData.outputs.push(endpoint.toJSON());
		});		

		return my.jsonData;
	}
	that.toJSON = toJSON;
	
	// Everything added to the public interface can be accessed from the
	// private interface too
	$.extend(my,that);
	
	return that;
}
