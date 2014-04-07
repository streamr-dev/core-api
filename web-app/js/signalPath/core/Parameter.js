SignalPath.ParamRenderers = {
		"default": {
			create: function(module,data) {
				if (data.possibleValues) {
					var select = $("<select class='parameterInput' style='visibility:hidden'></select>");
					$(data.possibleValues).each(function(i,val) {
						var option = $("<option></option>");
						option.attr("value",val.value);
						option.append(val.name);
						
						if (data.value==val.value)
							option.attr("selected","selected");
						
						select.append(option);
					});
					
					select.on("spIOReady",(function(s) {
						return function() {
							s.chosen({width:"180px", search_contains:true}).css('visibility', 'visible');
						};
					})(select));
					
					return select;
				}
				else {
					var input = $("<input class='parameterInput' type='text' value='"+data.value+"'>");
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
				var search = $("<input class='parameterInput' type='text' style='"+(data.value ? "display:none" : "")+"' class='streamSearch' value='"+(data.streamName || "")+"'>");
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
							$(sym).find("a").html(ui.item.name);

							if (mod!=null)
								SignalPath.updateModule(mod);
							else {
								$(sch).hide();
								$(sym).show();
							}
						}
						else {
							$(sch).hide();
							$(sym).show();
						}
					}
				})(symbol,search,id,module);
				
				$(search).autocomplete({
					source: function(request, callback) {
						$.ajax({
							url: project_webroot+"stream/search", 
							data: {
								term: request.term,
								module: module.getModuleId()
							},
							dataType: 'json',
							success: callback,
							error: function(jqXHR, textStatus, errorThrown) {
								SignalPath.options.errorHandler({msg: errorThrown});
								callback([]);
							}
						});
					},
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


SignalPath.Parameter = function(json, parentDiv, module, type, my) {
	my = my || {};
	my = SignalPath.Input(json, parentDiv, module, type || "parameter input", my);
	
	var super_createDiv = my.createDiv;
	my.createDiv = function() {
		var div = super_createDiv();

		// Create the parameter input form
		my.input = createParamInput();
		var inputTd = $("<td></td>");
		inputTd.append(my.input);
		parentDiv.parent().append(inputTd);
		
		// Assign disabled class to input when the parameter is connected
		div.bind("spConnect", (function(me) {
			return function(output) {
				me.input.attr("disabled","disabled").addClass("disabled");
			}
		})(my));
		
		div.bind("spDisconnect", (function(me) {
			return function(output) {
				me.input.removeAttr("disabled").removeClass("disabled");
			}
		})(my));
		
		// Changes to parameters can be made at runtime
		my.input.change(function() {
			if (SignalPath.isRunning() && SignalPath.options.allowRuntimeChanges && confirm("Make a runtime change to '"+my.getDisplayName()+"'?")) {
				var value = getParamRenderer(my.json).getValue(my.module, my.json, my.input);
				SignalPath.sendUIAction(module.getHash(), {type:"paramChange", param:my.getName(), value:value}, function(resp) {});
			}
		});
		
		// Trigger the spIOReady event on the input as well
		my.input.trigger("spIOReady");
		
		return div;
	}
	
	// PRIVATE FUNCTIONS
	function getParamRenderer() {
		return SignalPath.getParamRenderer(my.json);
	}
	
	function createParamInput() {
		var result = null;

		var renderer = getParamRenderer(my.json);
		result = renderer.create(my.module, my.json);
		
		if (my.json.updateOnChange) {
			$(result).change(function() {
				SignalPath.updateModule(my.module);
			});
		}
		
		return result;
	}
	
	var super_toJSON = my.toJSON;
	my.toJSON = function() {
		my.json.value = getParamRenderer(my.json).getValue(my.module, my.json, my.input);
		return super_toJSON();
	}
	
	return my;
}