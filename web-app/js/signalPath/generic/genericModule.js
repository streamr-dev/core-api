SignalPath.ParamRenderers = {
		"default": {
			create: function(module,data) {
				var input = $("<input type='text' value='"+data.value+"'>");
				return input;
			},
			getValue: function(module,data,input) {
				return $(input).val();
			}
		},
		"Stream": {
			create: function(module,data) {
				var span = $("<span></span>");
				
				// Show search if no value is selected
				var search = $("<input type='text' style='"+(data.value ? "display:none" : "")+"' class='streamSearch' value='"+(data.streamName || "")+"'>");
				span.append(search);
				
				// Show symbol if it exists
				var symbol = $("<span class='streamName' style='"+(data.value ? "" : "display:none")+"'><a href='#'>"+data.streamName+"</a></span>");
				var id = $("<input type='hidden' class='streamId' value='"+data.value+"'>");
					
				span.append(symbol);
				span.append(id);
					
				symbol.click((function(sym,sch) {
					return function() {
						$(sym).hide();
						$(sch).show();
						return false;
					}
				})(symbol,search));
					
				var onSel = (function(sym,sch,id,mod) {
					return function(event,ui) {
						if (ui.item) {
							$(id).val(ui.item.id);
							SignalPath.updateModule(mod);
						}
						else {
							$(sch).hide();
							$(sym).show();
						}
					}
				})(symbol,search,id,module);
				
				$(search).autocomplete({
					// TODO: generalize to Streams
					source: "jsonGetOrderBook",
					minLength: 2,
					select: onSel
				}).data("autocomplete")._renderItem = function(ul,item) {
					return $("<li></li>")
						.data("item.autocomplete",item)
						.append("<a>"+item.name+"</a>")
						.appendTo(ul);
				};
					
				return span;
			},
			getValue: function(module,data,input) {
				var hidden = $(input).find("input.streamId");
				return hidden.val();
			}
		}
}

// Bind connection and disconnection events
jsPlumb.bind("jsPlumbConnection",function(connection) {
	$(connection.source).trigger("spConnect", connection.target);
	$(connection.target).trigger("spConnect", connection.source);
});
jsPlumb.bind("jsPlumbConnectionDetached",function(connection) {
	$(connection.source).trigger("spDisconnect", connection.target);
	$(connection.target).trigger("spDisconnect", connection.source);
});

SignalPath.GenericModule = function(data, canvas, my) {
	my = my || {};
	
	var that = SignalPath.EmptyModule(data, canvas, my);

	my.paramTable = null;
	my.ioTable = null;

	my.enableIONameChange = true;
	
	// PRIVATE FUNCTIONS
	function getParamRenderer(data) {
		var key = (data.type ? data.type : "default");
		var renderer = (key in SignalPath.ParamRenderers ? SignalPath.ParamRenderers[key] : SignalPath.ParamRenderers["default"]);
		return renderer;
	}
	
	function createParamInput(data) {
		var result = null;
		
		if (data.possibleValues) {
			var select = $("<select></select>");
			$(data.possibleValues).each(function(i,val) {
				var option = $("<option></option>");
				option.attr("value",val.value);
				option.append(val.name);
				
				if (data.value==val.value)
					option.attr("selected","selected");
				
				select.append(option);
			});
			result = select;
		}
		else {
			var renderer = getParamRenderer(data);
			result = renderer.create(that,data);
//			var input = $("<input type='text' value='"+data.value+"'>");
//			result = input;
		}
		
		if (data.updateOnChange) {
			$(result).change(function() {
				SignalPath.updateModule(that);
			});
		}
		
		return result;
	}
	
	function autoConnectInputs() {
		var outputs = $("div.output");
		var oIdx = 0;
		$(my.div).find("div.input").each(function(i,input) {
			// Skip parameters
			if (!$(input).hasClass("parameter")) {
				// Is this input connected?
				var connections = jsPlumb.getConnections({source:input.attr("id"), scope:"*"});
				if (connections.length==0) {
					// Find an unconnected output
					while (oIdx<outputs.length) {
						var output = outputs[oIdx];
						var connections = jsPlumb.getConnections({target:output.attr("id"), scope:"*"});
						if (connections.length==0) {
							jsPlumb.connect({source:input, target:output});
							break;
						}
						oIdx++;
					}
				}
			}
		});
	}
	
	function createModuleSettings() {
		// Button for toggling the clearState. Default true.
		
		if (my.jsonData.canClearState==null || my.jsonData.canClearState) {
			var clear = $("<span class='moduleSwitch clearState'>CLEAR</span>");
			if (my.jsonData.clearState==null)
				my.jsonData.clearState = true;

			clear.addClass(my.jsonData.clearState ? "ioSwitchTrue" : "ioSwitchFalse");
			my.body.append(clear);
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
	
	function generateId() {
		var id = "myId_"+my.hash+"_"+new Date().getTime();
		
		// Check for clashes
		while ($("#"+id).length>0)
			id += "_";
				
		return id;
	}
	
	function checkConnection(connection) {
		// Endpoints must contain at least one acceptedType in common
		var arr1 = $("#"+connection.sourceId).data("acceptedTypes");
		var arr2 = $("#"+connection.targetId).data("acceptedTypes");
		
		for(var i = 0; i<arr1.length; i++)
		    for(var j=0; j<arr2.length; j++)
		        if(arr1[i]==arr2[j])
		            return true;

		return false;
	}
	
	function addEndpoint(json,connDiv,id) {
		
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
		var ep = jsPlumb.addEndpoint(connDiv, {
				isSource:isInput, 
				isTarget:isOutput,
				dragOptions: {},
				dropOptions: {},
				anchor: (isInput ? [0, 0.5, -1, 0, -15, 0] : [1, 0.5, 1, 0, 15, 0]),
				maxConnections: (isInput?1:-1),
				beforeDrop: checkConnection
//				scope:scope
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
	
	function autoConnectOutputByExample(output) {
		var connections = jsPlumb.getConnections({target:output.attr("id")});
		
		if (connections.length>0) {
			var targets = [];
			
			$(connections).each(function(j,connection) {
				var moduleName = connection.endpoints[0].element.parents(".component").find(".modulename").text();
				var inputName = connection.endpoints[0].element.children(".ioname").text();
				targets.push({
					output: output,
					moduleName: moduleName,
					inputName: inputName
				});
			});
			
			$(targets).each(function(j,target) {
				// Get all module names
				var modules = $(".component");
				$(modules).each(function(i,module) {
					var moduleName = $(module).find(".modulename").text();
					// If the module name is a match, look for an input with the correct name
					if (moduleName==target.moduleName) {
						var inputs = $(module).find("div.input");
						$(inputs).each(function(k,input) {
							var inputName = $(input).find(".ioname").text();
							var connections = jsPlumb.getConnections({source:input.attr("id")});
							if (inputName==target.inputName && connections.length==0)
								jsPlumb.connect({source:input, target:target.output});
						});
					}
				});
			});
		}
	}
	
	function createInputSettings(div,data) {
		
		// Driving input. Default true.
		
		if (data.canBeDrivingInput==null || data.canBeDrivingInput) {
			var driving = $("<span class='ioSwitch ioSwitchInput drivingInput'>DR</span>");
			if (data.drivingInput==null)
				data.drivingInput = (div.hasClass("parameter") ? false : true);
			driving.addClass(data.drivingInput ? "ioSwitchTrue" : "ioSwitchFalse");
			div.append(driving);
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
			var iv = $("<span class='ioSwitch ioSwitchInput initialValue'></span>");
			div.append(iv);
			
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
			var fb = $("<span class='ioSwitch ioSwitchInput feedback'>FB</span>");
			if (data.feedback==null)
				data.feedback = false;
			fb.addClass(data.feedback ? "ioSwitchTrue" : "ioSwitchFalse");
			div.append(fb);
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
		
		addExportToggle(div,data,"ioSwitchInput")
	}
	
	function addExportToggle(div,data,cls) {
		// Export toggle
		var ex = $("<span class='ioSwitch "+cls+" export' style='display:none'>EX</span>");

		ex.addClass(data.export ? "ioSwitchTrue" : "ioSwitchFalse");
		if (data.export)
			div.addClass("export");
		
		div.append(ex);
		ex.click(function() {
			if (data.export) {
				data.export = false;
				ex.removeClass("ioSwitchTrue");
				ex.addClass("ioSwitchFalse");

				div.removeClass("export");
			}
			else {
				data.export = true;
				ex.addClass("ioSwitchTrue");
				ex.removeClass("ioSwitchFalse");
				
				div.addClass("export");
			}
		});		
	}
	
	function createOutputSettings(div,data) {
		
		// NoRepeat. Default true. Add only for TimeSeries type
		if (data.type=="Double" && (data.canBeNoRepeat==null || data.canBeNoRepeat)) {
			var noRepeat = $("<span class='ioSwitch ioSwitchOutput noRepeat'>NR</span>");
			if (data.noRepeat==null)
				data.noRepeat = true;
			noRepeat.addClass(data.noRepeat ? "ioSwitchTrue" : "ioSwitchFalse");
			div.append(noRepeat);
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
		
		addExportToggle(div,data,"ioSwitchOutput");
	}
	
	function bindNameChange(ioname,data) {
		ioname.click(
				(function(data,ioname) {
					return function() {
						var n = $(ioname).text();

//						// Find input from json data
//						var jsonInput = null;
//						$(my.jsonData.inputs).each(function(i,jsI) {
//							if (jsI.name==n)
//								jsonInput=jsI;
//						});

						var displayName = prompt("Display name for "+data.name+":",n);
						if (displayName != null) {
							if (displayName != "" && displayName != data.name) {
								data.displayName = displayName;
							}
							else {
								delete data.displayName;
								displayName = data.name;
							}

							ioname.html(displayName);
						}
					}
				})(data,ioname)
		);
	}
	
	// PROTECTED FUNCTIONS
	
	var superUpdateFrom = that.updateFrom;
	function updateFrom(data) {
		var oldInputConnections = [];
		var oldOutputConnections = [];
		
		$(my.div).find("div.input").each(function(i,input) {
			var ioName = $(input).find(".ioname").text();
			var connections = jsPlumb.getConnections({source:$(input).attr("id"), scope:"*"});
			if (connections.length>0) {
				$(connections).each(function(j,c) {
					oldInputConnections.push({
						name: ioName,
						endpoint: c.endpoints[1] // target endpoint
					});
				});
			}
		});
		
		$(my.div).find("div.output").each(function(i,output) {
			var ioName = $(output).find(".ioname").text();
			var connections = jsPlumb.getConnections({target:$(output).attr("id"), scope:"*"});
			if (connections.length>0) {
				$(connections).each(function(j,c) {
					oldOutputConnections.push({
						name: ioName,
						endpoint: c.endpoints[0] // source endpoint
					});
				});
			}
		});
		
		superUpdateFrom(data);

		
		// Reconnect io by name
		$(oldInputConnections).each(function(i,item) {
			// Find an input by that name
			var found = null;
			$(my.div).find("div.input span.ioname").each(function(j,ioname) {
				if ($(ioname).text()==item.name)
					found = $(ioname).parent();
			});
			if (found) {
				jsPlumb.connect({source:found.data("endpoint"), target:item.endpoint});
			}
		});
		
		$(oldOutputConnections).each(function(i,item) {
			// Find an input by that name
			var found = null;
			$(my.div).find("div.output span.ioname").each(function(j,ioname) {
				if ($(ioname).text()==item.name)
					found = $(ioname).parent();
			});
			if (found) {
				jsPlumb.connect({source:item.endpoint, target:found.data("endpoint")});
			}
		});
		
		jsPlumb.repaint($(my.div).find("div.input"));
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
		
		// Disconnect button
		var disconnectLink = $("<span class='disconnect modulebutton ui-corner-all ui-icon ui-icon-cancel'></span>");
		disconnectLink.click(function() {
			my.disconnect();
		});
		my.header.append(disconnectLink);
		
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
		
		// Bind connection and disconnection events
//		jsPlumb.bind("jsPlumbConnection",function(connection) {
//			my.onConnect(connection.source, connection.target);
//		});
//		jsPlumb.bind("jsPlumbConnectionDetached",function(connection) {
//			my.onDisconnect(connection.source);
//		});

//		my.positionConnections();

		createModuleSettings();
		
		my.title.dblclick(autoConnectInputs);
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
		});
		$(my.div).find("div.output").each(function(i,div) {
			jsPlumb.removeAllEndpoints(div);
		});
		
		superClose();
	}
	that.close = close;
	
	function getInput(name) {
		$(my.div).find("div.input").each(function(i,input) {
			var n = $(input).find("span.ioname").text();
			if (n==name) {
				return input;
			}
		});
	}
	my.getInput = getInput;
	
	function getOutput(name) {
		$(my.div).find("div.output").each(function(i,output) {
			var n = $(input).find("span.ioname").text();
			if (n==name) {
				return output;
			}
		});
	}
	my.getOutput = getOutput;
	
	function addParameter(p) {
		var row = $("<tr></tr>");
		var paramTd = $("<td></td>");
		var param = $("<div class='parameter input "+p.type+"'></div>");
		param.data("name",p.name);
		
		var ioname = $("<span class='ioname'>"+(p.displayName ? p.displayName : p.name)+"</span>");
		param.append(ioname);
		
		if (my.enableIONameChange) 
			bindNameChange(ioname,p);
		
		var input = createParamInput(p);
		var inputTd = $("<td></td>");

		param.data("input",input);
		my.div.data("param."+p.name,param);

		paramTd.append(param);
		row.append(paramTd);

		inputTd.append(input);
		row.append(inputTd);
		my.paramTable.append(row);
		
		if (p.canConnect==null || p.canConnect) {
			addEndpoint(p,param,p.endpointId);

			param.bind("spConnect", (function(me) {
				return function(output) {
					me.data("input").attr("disabled","disabled").addClass("disabled");
				}
			})(param));
			
			param.bind("spDisconnect", (function(me) {
				return function(output) {
					me.data("input").removeAttr("disabled").removeClass("disabled");
				}
			})(param));
		}

//		if (p.export)
//			param.addClass("export");
		
		createInputSettings(param,p);
	}
	my.addParameter = addParameter;
	
	function addIO(data,type) {
		// Find first empty input in the my.ioTable
		var td = my.ioTable.find("td."+type+":empty:first");
		// Add row if no suitable row found
		if (td==null || td.length == 0) {
			var newRow = $("<tr><td class='input'></td><td class='output'></td>");
			my.ioTable.append(newRow);
			td = newRow.find("td."+type+":first");
		}

		// Create connection div
		var div = $("<div class='"+type+" "+data.type+"'></div>");
		div.data("name",data.name);
		my.div.data(type+"."+data.name,div);
		
		var ioname = $("<span class='ioname'>"+(data.displayName ? data.displayName : data.name)+"</span>");
		div.append(ioname);
		
		if (my.enableIONameChange)
			bindNameChange(ioname,data);
		
		td.append(div);
		
		// Link json data to the div
		div.data("json",data);

//		if (data.export)
//			div.addClass("export");
		
		addEndpoint(data, div, type=="input" ? data.endpointId : data.id);
		
		// Ensure that the jsonData contains this input name
		var found = false;
		var arr = (type=="input" ? my.jsonData.inputs : my.jsonData.outputs);
		for (var i=0;i<arr.length;i++) {
			if (arr[i].name == data.name) {
				found = true;
				break;
			}
		}
		if (!found)
			arr.push(data);

		// Create io settings
		if (type=="input")
			createInputSettings(div,data);
		else createOutputSettings(div,data);
		
		return div;
	}
	my.addIO = addIO;
	
	my.addInput = function(data) {
		var div = my.addIO(data,"input");
		
		if (!data.suppressWarnings) {
			div.data("endpoint").setStyle(jQuery.extend({},jsPlumb.Defaults.EndpointStyle,{strokeStyle:"red"}));

			// Add/remove a warning class to unconnected inputs
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
		
		return div;
	}

	my.addOutput = function(data) {
		return my.addIO(data,"output");
	}
	
//	var superOnDrag = my.onDrag;
//	function onDrag() {
//		superOnDrag();
//		my.positionConnections();
//	}
//	my.onDrag = onDrag;
	
//	my.onConnect = function(div,sourceDiv) {
//		if (div.hasClass("parameter"))
//			div.data("input").attr("disabled","disabled").addClass("disabled");
//	}
//
//	my.onDisconnect = function(div) {
//		if (div.hasClass("parameter"))
//			div.data("input").removeAttr("disabled").removeClass("disabled");
//	}
	
//	var superRedraw = that.redraw;
//	function redraw() {
////		my.positionConnections();
//		superRedraw();
//	}
//	that.redraw = redraw;
		
	var superToJSON = that.toJSON;
	function toJSON() {
		my.jsonData = superToJSON();

		// Update parameter values
		$(my.jsonData.params).each(function(i,param) {
			var paramDiv = my.div.data("param."+param.name);
			var input = paramDiv.data("input");
			var value = getParamRenderer(param).getValue(that,param,input);
			param.value = value;

			if (input.attr("disabled")) {
				param.connected = true;
				var connection = jsPlumb.getConnections({source:paramDiv.attr("id"), scope:"*"})[0];
				param.endpointId = connection.sourceId;
				param.sourceId = connection.targetId;
			}
			else param.connected = false;

//			if (paramDiv.hasClass("export"))
//				param.export = true;
//			else param.export = false;
		});

		// Update inputs and outputs
		$(my.jsonData.inputs).each(function(i,input) {
			var inputDiv = my.div.data("input."+input.name);

			var connection = jsPlumb.getConnections({source:inputDiv.attr("id"), scope:"*"})[0];

			if (connection) {
				input.connected = true;
				input.endpointId = connection.sourceId;
				input.sourceId = connection.targetId;
			}
			else input.connected = false;

//			if (inputDiv.hasClass("export"))
//				input.export = true;
//			else input.export = false;
		});

		$(my.jsonData.outputs).each(function(i,output) {
			var outputDiv = my.div.data("output."+output.name);

			var connections = jsPlumb.getConnections({target:outputDiv.attr("id"), scope:"*"});

			if (connections.length>0) {
				output.connected = true;
				output.id = outputDiv.attr("id");
				output.targets = [];
				$(connections).each(function(j,connection) {
					output.targets.push(connection.sourceId);
				});
			}
			else output.connected = false;

//			if (outputDiv.hasClass("export"))
//				output.export = true;
//			else output.export = false;

		});

		return my.jsonData;
	}
	that.toJSON = toJSON;
	
	/*
	 * SHOULD NOT (NEED TO) DECLARE NEW PUBLIC METHODS IN SUBCLASSES OF EmptyModule
	that.positionConnections = my.positionConnections;
	
	that.addIO = my.addIO;
	
	that.addInput = my.addInput;
	
	that.addOutput = my.addOutput;
	
	that.onConnect = my.onConnect;
		
	that.onDisconnect = my.onDisconnect;
	
	*/
		
	return that;
}

