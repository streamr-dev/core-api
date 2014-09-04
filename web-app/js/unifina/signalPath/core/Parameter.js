SignalPath.ParamRenderers = {
		"default": {
			create: function(module,data) {
				if (data.possibleValues) {
					var select = $("<select class='parameterInput form-control'></select>");
					$(data.possibleValues).each(function(i,val) {
						var option = $("<option></option>");
						option.attr("value",val.value);
						option.append(val.name);
						
						if (data.value==val.value)
							option.attr("selected","selected");
						
						select.append(option);
					});
					
					return select;
				}
				else {
					var input = $("<input class='parameterInput form-control' type='text' value='"+data.value+"'>");
					return input;
				}
			},
			getValue: function(module,data,input) {
				return $(input).val();
			},
			getValueName: function(module,data,input) {
				return $(input).val();
			}
		},
		"Stream": {
			create: function(module,data) {
				var span = $("<span></span>");
				
				// Show search if no value is selected
				var search = $("<input type='text' style='"+(data.value ? "display:none" : "")+"' class='parameterInput streamSearch form-control' value='"+(data.streamName || "")+"'>");
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
					
				var onSel = (function(sym,sch,id,mod,d) {
					return function(event,item) {
						if (item) {
							// If the current module corresponds to the selected feed module and the new one
							// does not, the module needs to be replaced
							if (mod && d.checkModuleId && mod.getModuleId() != item.module) {
								if (confirm("This stream is implemented by a different module. Replace current module? This will break current connections.")) {
									SignalPath.replaceModule(mod, item.module,{params:[{name:"stream", value:item.id}]});
								}
							}
							// Handle same module implementation
							else {
								$(id).val(item.id);
								$(sym).find("a").html(item.name);
	
								if (mod!=null)
									SignalPath.updateModule(mod);
								else {
									$(sch).hide();
									$(sym).show();
								}
							}
						}
						else {
							$(sch).hide();
							$(sym).show();
						}
					}
				})(symbol,search,id,module,data);
				
				$(search).typeahead({
					highlight: true,
					hint: false,
					minLength: 1,
				}, {
					name: 'streams',
					displayKey: 'name',
					source: function(q, callback) {
						$.ajax({
							url: project_webroot+"stream/search", 
							data: { term: q },
							dataType: 'json',
							success: callback,
							error: function(jqXHR, textStatus, errorThrown) {
								SignalPath.options.errorHandler({msg: errorThrown});
								callback([]);
							}
						});
					}
				})
				.on('typeahead:selected', onSel)

				/*.data("autocomplete")._renderItem = function(ul,item) {
					return $("<li></li>")
						.data("item.autocomplete",item)
						.append("<a>"+item.name+"</a>")
						.appendTo(ul);
				};*/
					
				return span;
			},
			getValue: function(module,data,input) {
				var hidden = $(input).find("input.streamId");
				return hidden.val();
			},
			getValueName: function(module,data,input) {
				var text = $(input).find("span.streamName a").text();
				return text;
			}
		}
}

SignalPath.getParamRenderer = function(data) {
	var key = (data.type ? data.type : "default");
	var renderer = (key in SignalPath.ParamRenderers ? SignalPath.ParamRenderers[key] : SignalPath.ParamRenderers["default"]);
	return renderer;
};


SignalPath.Parameter = function(json, parentDiv, module, type, pub) {
	pub = pub || {};
	pub = SignalPath.Input(json, parentDiv, module, type || "parameter input", pub);
	
	var super_createDiv = pub.createDiv;
	pub.createDiv = function() {
		var div = super_createDiv();

		// Create the parameter input form
		pub.input = createParamInput();
		var inputTd = $("<td></td>");
		inputTd.append(pub.input);
		parentDiv.parent().append(inputTd);
		
		// Assign disabled class to input when the parameter is connected
		div.bind("spConnect", (function(me) {
			return function(output) {
				me.input.attr("disabled","disabled");
			}
		})(pub));
		
		div.bind("spDisconnect", (function(me) {
			return function(output) {
				me.input.removeAttr("disabled");
			}
		})(pub));
		
		// Changes to parameters can be made at runtime
		pub.input.change(function() {
			if (SignalPath.isRunning() && SignalPath.options.allowRuntimeChanges && confirm("Make a runtime change to '"+pub.getDisplayName()+"'?")) {
				var value = getParamRenderer(pub.json).getValue(pub.module, pub.json, pub.input);
				SignalPath.sendUIAction(module.getHash(), {type:"paramChange", param:pub.getName(), value:value}, function(resp) {});
			}
		});
		
		// Trigger the spIOReady event on the input as well
		pub.input.trigger("spIOReady");
		
		return div;
	}

	// PRIVATE FUNCTIONS
	function getParamRenderer() {
		return SignalPath.getParamRenderer(pub.json);
	}
	
	function createParamInput() {
		var result = null;

		var renderer = getParamRenderer(pub.json);
		result = renderer.create(pub.module, pub.json);
		
		if (pub.json.updateOnChange) {
			$(result).change(function() {
				SignalPath.updateModule(pub.module);
			});
		}
		
		return result;
	}
	
	var super_toJSON = pub.toJSON;
	pub.toJSON = function() {
		pub.json.value = getParamRenderer(pub.json).getValue(pub.module, pub.json, pub.input);
		return super_toJSON();
	}
	
	return pub;
}