(function() {

	/**
	 * A parameter view that renders an input field for the parameter value.
	 * Numeric types will be parsed, otherwise the value will be interpreted as a string.
	 * If the model has possibleValues set, then a dropdown with the possible values
	 * is shown.
	 *
	 * @param parentElement The parent dom element
	 * @param parameter The parameter this editor is attached to
	 * @constructor
     */
	var DefaultParameterValueEditor = function(parentElement, parameter) {
		this.parameter = parameter
		this.data = this.parameter.json

		this.element = this.createElement()
		parentElement.append(this.element)
	}

	/**
	 * Creates and returns the element that shows the value of this parameter
	 *
	 * @returns {jQuery|HTMLElement}
     */
	DefaultParameterValueEditor.prototype.createElement = function() {
		var _this = this
		var result

		if (this.data.possibleValues) {
			var select = $("<select class='parameterInput form-control'></select>");

			$(this.data.possibleValues).each(function(i,val) {
				var option = $("<option></option>");
				option.attr("value",val.value);
				option.append(val.name);

				if (_this.data.value && _this.data.value.toString() == val.value)
					option.attr("selected","selected");

				select.append(option);
			});

			result = select
		}
		else if (this.data.isTextArea) {
			result = $("<textarea class='parameterInput form-control' />");
			result.val(this.data.value);
		}
		else {
			result = $("<input class='parameterInput form-control' type='text' />");
			result.val(this.data.value);
		}

		result.change(function() {
			$(_this).trigger('change')
		})

		return result
	}

	/**
	 * Gets the current value of this parameter
     */
	DefaultParameterValueEditor.prototype.getValue = function() {
		if (this.data.type === "Double" || this.data.type === "Integer" || this.data.type === "Number")
			return parseFloat($(this.element).val())
		else return $(this.element).val();
	}

	/**
	 * Gets a label for the current value of this parameter (as string)
	 */
	DefaultParameterValueEditor.prototype.getValueName = function() {
		return this.getValue().toString()
	}

	DefaultParameterValueEditor.prototype.disable = function() {
		this.element.attr("disabled","disabled");
	}

	DefaultParameterValueEditor.prototype.enable = function() {
		this.element.removeAttr("disabled");
	}

	/**
	 * View for parameters of type Stream
	 *
	 * @param parentElement The parent dom element
	 * @param parameter The parameter this editor is attached to
     * @constructor
     */
	var StreamParameterValueEditor = function(parentElement, parameter) {
		DefaultParameterValueEditor.call(this, parentElement, parameter)
	}
	StreamParameterValueEditor.prototype = Object.create(DefaultParameterValueEditor.prototype)

	StreamParameterValueEditor.prototype.createElement = function() {
		var _this = this

		var span = $("<span class='stream-parameter-wrapper'></span>");

		// Show search if no value is selected
		var search = $("<input type='text' style='"+(this.data.value ? "display:none" : "")+"' class='parameterInput streamSearch form-control' value='"+(this.data.streamName || "")+"'>");
		this.search = search
		span.append(search);

		// Show name if a value is set
		this.label = $("<span class='streamName' style='"+(this.data.value ? "" : "display:none")+"'><a href='#'>"+this.data.streamName+"</a></span>");
		span.append(this.label);

		this.label.click(function() {
			$(_this.label).hide();
			$(_this.search).show();
			return false;
		})

		var onSel = function(event,item) {
			if (item) {
				// If the current module corresponds to the selected feed module and the new one
				// does not, the module needs to be replaced
				if (_this.parameter.module && _this.data.checkModuleId && _this.parameter.module.getModuleId() != item.module) {
					bootbox.confirm("This stream is implemented by a different module. Replace current module? This will break current connections.", function(result) {
						if (result)
							SignalPath.replaceModule(_this.parameter.module, item.module,{params:[{name:"stream", value:item.id}]});
					});
				}
				// Handle same module implementation
				else {
					_this.data.value = item.id
					_this.label.find("a").html(item.name)
					_this.data.streamName = item.name

					$(_this.search).hide()
					$(_this.label).show()
				}
			}
			else {
				$(_this.search).hide();
				$(_this.label).show();
			}
			$(_this).trigger('change')
		}

		var searchParams = {}

		if (this.data.feedFilter)
			searchParams.feed = this.data.feedFilter

		$(search).typeahead({
				highlight: true,
				hint: false,
				minLength: 1
			}, {
				name: 'streams',
				displayKey: 'name',
				source: function(q, callback) {
					$.ajax({
						url: Streamr.projectWebroot+"stream/search",
						data: $.extend({}, searchParams, { term: q }),
						dataType: 'json',
						success: callback,
						error: function(jqXHR, textStatus, errorThrown) {
							SignalPath.options.errorHandler({msg: errorThrown});
							callback([]);
						}
					})
				},
				templates: {
					suggestion: function(item) {
						if (item.description)
							return"<p><span class='tt-suggestion-name'>"+item.name+"</span><br><span class='tt-suggestion-description'>"+item.description+"</span></p>"
						else return "<p><span class='tt-suggestion-name'>"+item.name+"</span></p>"
					}
				}
			})
			.on('typeahead:selected', onSel)

		return span;
	}

	StreamParameterValueEditor.prototype.getValue = function() {
		return this.data.value
	}

	StreamParameterValueEditor.prototype.getValueName = function() {
		return this.data.streamName
	}

	StreamParameterValueEditor.prototype.disable = function() {
		this.search.attr('disabled', 'disabled')
	}

	StreamParameterValueEditor.prototype.enable = function() {
		this.search.removeAttr('disabled')
	}

	/**
	 * Shows a colorpicker for params of type Color.
	 *
	 * @param parentElement The parent dom element
	 * @param parameter The parameter this editor is attached to
	 * @constructor
	 */
	var ColorParameterValueEditor = function(parentElement, parameter) {
		DefaultParameterValueEditor.call(this, parentElement, parameter)
	}
	ColorParameterValueEditor.prototype = Object.create(DefaultParameterValueEditor.prototype)

	ColorParameterValueEditor.prototype.createElement = function() {
		var _this = this

		var inputContainer = $("<div class='color-input-container'></div>")
		this.colorInput = $("<input type='text' class='parameterInput colorInput form-control'/>");
		inputContainer.append(this.colorInput)

		this.colorInput.spectrum({
			preferredFormat: "rgb",
			showInput: true,
			showButtons: false,
			hide: function() {
				var oldValue = _this.data.value
				_this.data.value = _this.getValue()
				if (oldValue !== _this.data.value) {
					$(_this).trigger('change')
				}
			}
		})
		this.colorInput.spectrum("set", this.data.value)

		return inputContainer
	}

	ColorParameterValueEditor.prototype.getValue = function() {
		return this.colorInput.spectrum("get").toRgbString()
	}

	ColorParameterValueEditor.prototype.disable = function() {
		this.colorInput.spectrum("disable")
	}

	ColorParameterValueEditor.prototype.enable = function() {
		this.colorInput.spectrum("enable")
	}

	/**
	 * Shows a key-value-pair editor for parameters of type Map
	 *
	 * @param parentElement The parent dom element
	 * @param parameter The parameter this editor is attached to
	 * @constructor
	 */
	var MapParameterValueEditor = function(parentElement, parameter) {
		DefaultParameterValueEditor.call(this, parentElement, parameter)
	}
	MapParameterValueEditor.prototype = Object.create(DefaultParameterValueEditor.prototype)

	MapParameterValueEditor.prototype.createElement = function() {
		var _this = this
		var div = $("<div/>")
		var collection = new KeyValuePairList()
		collection.fromJSON(this.data.value || {})

		this.editor = new KeyValuePairEditor({
			el: div,
			collection: collection
		})

		// collection add and remove will resize the component, so module needs to be redrawn
		collection.on('add', function() {
			_this.parameter.module.redraw()
			// don't trigger change on add, as the key and value will be empty
		})
		collection.on('remove', function() {
			_this.parameter.module.redraw()
			$(_this).trigger('change')
		})

		div.on('change', function() {
			$(_this).trigger('change')
		})

		return div
	}

	MapParameterValueEditor.prototype.getValue = function() {
		return this.editor.collection.toJSON()
	}

	MapParameterValueEditor.prototype.disable = function() {
		this.editor.disable()
	}

	MapParameterValueEditor.prototype.enable = function() {
		this.editor.enable()
	}



	// Map types to value editors. DefaultParameterValueEditor is the default.
	var editorMappings = {
		"Stream": StreamParameterValueEditor,
		"Color": ColorParameterValueEditor,
		"Map": MapParameterValueEditor
	}

	SignalPath.Parameter = function(json, parentDiv, module, type, pub) {
		pub = pub || {};
		pub = SignalPath.Input(json, parentDiv, module, type || "parameter input", pub);

		var super_createDiv = pub.createDiv;
		pub.createDiv = function() {
			var div = super_createDiv();

			// Create the parameter input form
			var inputTd = $("<td></td>");
			parentDiv.parent().append(inputTd);
			pub.editor = createParameterValueEditor(inputTd);

			// Disable editor on connect, enable on disconnect
			div.bind("spConnect", function() {
				pub.editor.disable()
			})
			div.bind("spDisconnect", function() {
				pub.editor.enable()
			})

			// Changes to parameters can be made at runtime
			$(pub.editor).change(function(event) {
				if (SignalPath.isRunning() && SignalPath.options.allowRuntimeChanges) {
					var value = pub.editor.getValue()
					bootbox.confirm({
						message: "Make a runtime change to '"+pub.getDisplayName()+"'?",
						className: "bootbox-sm",
						callback: function(result) {
							if (result) {
								SignalPath.sendRequest(
									module.getHash(),
									{
										type: "paramChange",
										param: pub.getName(),
										value: value
									},
									function (resp) {
									}
								);
							}
						}
					})
				}
			});

			if (pub.json.updateOnChange) {
				var oldVal = pub.json.value

				$(pub.editor).change(function() {
					var newVal = pub.toJSON().value
					if (newVal != oldVal && !SignalPath.isRunning()) { // != on purpose instead of !==
						oldVal = newVal
						SignalPath.updateModule(pub.module)
					}
				});
			}

			return div;
		}

		var super_getJSPlumbEndpointOptions = pub.getJSPlumbEndpointOptions;
		pub.getJSPlumbEndpointOptions = function(json,connDiv) {
			var opts = super_getJSPlumbEndpointOptions(json,connDiv);
			opts.cssClass = (opts.cssClass || "") + " jsPlumb_parameter";
			return opts;
		}

		function createParameterValueEditor(parentElement) {
			var editorClass = editorMappings[pub.json.type] || DefaultParameterValueEditor
			var Temp = function() {}
			var editor

			// see http://stackoverflow.com/questions/3362471/how-can-i-call-a-javascript-constructor-using-call-or-apply
			Temp.prototype = editorClass.prototype;
			editor = new Temp;
			editorClass.apply(editor, [parentElement, pub]);

			return editor;
		}

		pub.getValue = function() {
			return pub.editor.getValue();
		}

		var super_toJSON = pub.toJSON;
		pub.toJSON = function() {
			pub.json.value = pub.getValue();
			return super_toJSON();
		}

		return pub;
	}
})();
