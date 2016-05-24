/**
 * Events emitted on spObject:
 * updated - when eg. the stream is changed, updated is triggered with the new data
 */
SignalPath.EmptyModule = function(data, canvas, prot) {
	prot = prot || {};

	prot.div = null;
	prot.header = null;
	prot.title = null;
	prot.body = null;
	prot.resizable = false;
	
	prot.jsonData = data;
	prot.hash = prot.jsonData.hash;
	prot.type = prot.jsonData.type;
	prot.id = "module_"+prot.hash;

	var pub = {}
	var $prot = $(prot)

	prot.warnings = []

	prot.dragOptions = {
		drag: function(e, ui) {
			var cpos = canvas.offset()
			var x = ui.offset.left + canvas.scrollLeft()
			var y = ui.offset.top + canvas.scrollTop()
			
			if (x < cpos.left-100 || y < cpos.top-50) {
				return false
			}
		}
	}
	
	pub.getDragOptions = function() {
		return prot.dragOptions;
	}
	
	function createDiv() {
		var buttons = []

		prot.div = $("<div id='"+prot.id+"' class='component context-menu "+prot.type+"'></div>");
		prot.div.data("spObject",prot);
		
		// Set absolute position
		prot.div.css("position","absolute");
		
		// Read position and width/height if saved
		if (prot.jsonData.layout) {
			loadPosition();
		} 
		// Else add to default position in viewport
		else {
			prot.div.css('top',canvas.scrollTop() + 10);
			prot.div.css('left',canvas.scrollLeft() + 10);
		}
		
		// Module header
		prot.header = $("<div class='moduleheader'></div>");
		prot.div.append(prot.header);
		
		// Create title
		prot.title = $("<span class='modulename'>"+prot.jsonData.name+"</span>");
		prot.header.append(prot.title);
	
		// Close button
		var deleteLink = createModuleButton("delete fa-times")
		deleteLink.click(function() {
			pub.close();
		});

		buttons.push(deleteLink);

		// Help button shows normal help on hover and "extended" help in a dialog on click
		var helpLink = createModuleButton("help fa-question");

		var tooltipOptions = {
			animation: false,
			trigger: 'manual',
			container: SignalPath.getParentElement(),
			viewport: {
				selector: SignalPath.getParentElement(),
				padding: 0
			},
			html: true,
			placement: 'auto top',
			template: '<div class="tooltip" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner modulehelp-tooltip"></div></div>'
		}
		
		var delay=500, tooltipDelayTimer		
		helpLink.mouseenter(function() {
			tooltipDelayTimer = setTimeout(function() {
	 			prot.getHelp(false, function(htext) {
	 				tooltipOptions.title = htext
					// show tooltip after help text is loaded
					helpLink.tooltip(tooltipOptions)

					helpLink.on('shown.bs.tooltip', function(event, param) {
						$tt = $(".tooltip")
						if ($tt.length) {
							var top = $tt.offset().top
							// Workaround for CORE-216: tooltip is shown under main navbar
							if (top < 75 && $tt.hasClass("top")) {
								helpLink.tooltip("destroy")
								helpLink.tooltip($.extend({}, tooltipOptions, {placement: 'bottom'}))
								helpLink.tooltip("show")
							}
							MathJax.Hub.Queue(["Typeset",MathJax.Hub,$tt[0]]);
							MathJax.Hub.Queue(function(){
								$tt.find(".math-tex").addClass("math-jax-ready")
							})
						}
					})
					helpLink.tooltip('show')
	 			})
			}, delay)
		}).mouseleave(function() {
			clearTimeout(tooltipDelayTimer)
			helpLink.tooltip('destroy')
		})

		helpLink.click(function() {
			prot.getHelp(true, function(helptext) {
				var bb = bootbox.dialog({
					message: '<div class="modulehelp">'+helptext+'</div>',
					onEscape: function() { return true },
					animate: false,
					title: prot.jsonData.name,
					show: false,
					className: "module-help-dialog"
				})
				bb.on("shown.bs.modal", function(){
					MathJax.Hub.Queue(["Typeset",MathJax.Hub,bb.find(".modal-body")[0]]);
					MathJax.Hub.Queue(function(){
						bb.find(".modal-body .math-tex").addClass("math-jax-ready")
					})
				})
				bb.modal("show")
			})
		})

		buttons.push(helpLink);

		prot.body = $("<div class='modulebody'></div>");
		prot.div.append(prot.body);
		
		// If there are options, create options editor
		if (prot.jsonData.options) {
			var editOptions = createModuleButton("options fa-wrench");
			
			editOptions.click(function() {
				var $optionEditor = $("<div class='optionEditor'></div>");
				
				// Create and sort options by key
				var keys = Object.keys(prot.jsonData.options)
				keys.sort()

				// Create options
				keys.forEach(function(key) {
					// Create the option div
					var div = prot.createOption(key, prot.jsonData.options[key]);
					$optionEditor.append(div);
					// Store reference to the JSON option
					$(div).data("option",prot.jsonData.options[key]);
				})
				
				bootbox.dialog({
					animate: false,
					title: 'Options: '+prot.title.text(),
					message: $optionEditor,
					onEscape: function() { return true },
					buttons: {
						'Cancel': function() {},
						'OK': function() {
							$optionEditor.find(".option").each(function(i, div) {
								// Get reference to the JSON option
								prot.updateOption($(div).data("option"), div)
							})

							prot.onOptionsUpdated()
						}
					}
				})
			})

			buttons.push(editOptions)
		}
		
		if (prot.jsonData.canRefresh) {
			// If the module can refresh, add a refresh button
			var refresh = createModuleButton("refresh fa-refresh");
		
			refresh.click(function() {
				SignalPath.updateModule(pub);
			});
			buttons.push(refresh);
		}

		prot.header.append(buttons.reverse())
		
		// Must add to canvas before setting draggable
		canvas.append(prot.div);
		prot.div.addClass("draggable")
		prot.div.draggable(prot.dragOptions);
		
		prot.div.on("click dragstart", function(event) {
			$(".component.focus").each(function(i,c) {
				var ob = $(c).data("spObject");
				if (ob !== prot)
					ob.removeFocus()
			});
			prot.addFocus(true);
			event.stopPropagation();
		});
		
		prot.div.hover(function() {
			if (!prot.div.hasClass("focus")) {
				prot.addFocus(false);
			}
		}, function() {
			if (!prot.div.hasClass("holdFocus")) {
				prot.removeFocus();
			}
		});
		
		// A module is focused by default when it is created
		prot.addFocus(true);

		$(SignalPath).trigger('moduleAdded', [ prot.jsonData, prot.div ])

		return prot.div;
	}
	prot.createDiv = createDiv;
	
	function writePosition() {
		prot.jsonData.layout = jQuery.extend(prot.jsonData.layout || {}, {
			position: {
				top: prot.div.css('top'),
				left: prot.div.css('left')
			}
		});
	}
	prot.writePosition = writePosition;
	
	function loadPosition() {
		var item = prot.jsonData.layout;

		if (item.width)
			prot.div.css('width',item.width);
		if (item.height)
			prot.div.css('height',item.height);

		prot.div.css('top',item.position.top);
		prot.div.css('left',item.position.left);
	}
	prot.loadPosition = loadPosition;
	
	function setLayoutData(layout) {
		prot.jsonData.layout = layout;
		prot.loadPosition()
	}
	pub.setLayoutData = setLayoutData;
	
	function getLayoutData() {
		prot.writePosition();
		return prot.jsonData.layout;
	}
	pub.getLayoutData = getLayoutData;
	
	function getHelp(extended, cb) {
		if (prot.cachedHelpResponse)
			cb(prot.renderHelp(prot.cachedHelpResponse, extended))
		else {
	    	$.ajax({
			    type: 'GET',
			    url: Streamr.createLink({uri: 'api/v1/modules/'+prot.jsonData.id+'/help'}),
			    dataType: 'json',
			    success: function(data) {
			    	prot.cachedHelpResponse = data;
			    	cb(prot.renderHelp(prot.cachedHelpResponse, extended))
				},
			    error: function() {
			    	result = "An error occurred while loading module help.";
			    }
			});
		}
	}
	prot.getHelp = getHelp;
	
	function renderHelp(data, extended) {
		var result = "<p>"+(data && data.helpText ? data.helpText : "No help is available for this module.")+"</p>";
		
		var embedCode = prot.getEmbedCode()
		if (extended && embedCode) {
			result += "<div class='note note-info'>"
			result += "<p>To use this visualization on an external page, use the following tag:</p>";
			result += "<code>"+embedCode+"</code>"
			result += "</div>"
		}
		
		return result;
	}
	prot.renderHelp = renderHelp;
	
	prot.getEmbedCode = function() {
		if (SignalPath.isSaved() && prot.jsonData.uiChannel && prot.jsonData.uiChannel.webcomponent) {
			return "&lt;"+prot.jsonData.uiChannel.webcomponent+" canvas='"+SignalPath.getId()+"' module='"+prot.getHash()+"' /&gt;"
		}
		else return undefined;
	}
	
	function initResizable(options, element) {
		var defaultOptions = {
			helper: "chart-resize-helper"
		}
		options = $.extend({},defaultOptions,options || {});
		element = element || prot.div
		element.resizable(options);
		prot.resizable = true;
	}
	prot.initResizable = initResizable;
	
	function removeFocus() {
		prot.div.removeClass("focus");
		prot.div.removeClass("hoverFocus");
		prot.div.removeClass("holdFocus");
		prot.div.find(".showOnFocus").fadeTo(100,0);
	}
	prot.removeFocus = removeFocus;
	
	function addFocus(hold) {
		prot.div.addClass("focus");
		
		if (hold) prot.div.addClass("holdFocus");
		else prot.div.addClass("hoverFocus");
		
		prot.div.find(".showOnFocus").fadeTo(100,1);
	}
	prot.addFocus = addFocus;
	
	function createModuleButton(additionalClasses) {
		var button = $("<div class='modulebutton'><a class='btn btn-default btn-xs showOnFocus' href='#' style='padding: 0px'><i class='fa fa-fw "+(additionalClasses ? additionalClasses : "")+"'></span></div>");
		return button;
	}
	prot.createModuleButton = createModuleButton;
	
	function getContextMenu() {
		return [
		        {title: "Clone module", cmd: "clone"}
		]
	}
	prot.getContextMenu = getContextMenu;
	
	function handleContextMenuSelection(target,selection) {
		if (selection=="clone") {
			prot.clone();
		}
	}
	prot.handleContextMenuSelection = handleContextMenuSelection;
	
	/**
	 * Functions for rendering and parsing options
	 *
	 * For {possibleValues:[ {text: "Description", value: "X"}, ... ]} create a drop-down box
	 * For {type: "boolean"}, create drop-down with true/false
	 * For the rest, a free-text field
	 */
	function createOption(key, option) {
		var div = $("<div class='option'></div>");
		var title = $("<span class='optionTitle'>" + key + "</span>").appendTo(div);
		var value = $("<span class='optionValue'></span>").appendTo(div);

		if (option.possibleValues) {
			var $select = $("<select>");
			_(option.possibleValues).each(function(opt) {
				var $option = $("<option>");
				$option.attr("value", opt.value);
				$option.append(opt.text);
				if (option.value == opt.value) {
					$option.attr("selected", "selected");
				}
				$select.append($option);
			});
			value.append($select);
		} else if (option.type == "int" || option.type == "double" || option.type == "string") {
			var input = $("<input type='text'>");
			input.attr("value", option.value);
			value.append(input);
		} else if (option.type == "boolean") {
			value.append("<select><option value='true' " + (option.value ? "selected='selected'" : "") + ">true</option><option value='false' " + (!option.value ? "selected='selected'" : "") + ">false</option></select>");
		}
		return div;
	}
	prot.createOption = createOption;

	function updateOption(option, div) {
		var isDropdown = (option.type == "boolean" || option.possibleValues != undefined)
		var inputText = isDropdown ? $(div).find("select").val() : $(div).find("input").val()

		if (option.type == "int") {
			option.value = parseInt(inputText);
		} else if (option.type == "double") {
			option.value = parseFloat(inputText);
		} else if (option.type == "string") {
			option.value = inputText;
		} else if (option.type == "boolean") {
			option.value = (inputText == "true");
		}
	}
	prot.updateOption = updateOption;
	
	/**
	 * Called when options are updated
	 */
	function onOptionsUpdated() {
		// Reload the module
		SignalPath.updateModule(pub);
	}
	prot.onOptionsUpdated = onOptionsUpdated;
	
	function getHash() {
		return prot.hash;
	}
	pub.getHash = getHash;
	
	function getModuleId() {
		return prot.jsonData.id;
	}
	prot.getModuleId = getModuleId;
	
	function getDiv() {
		if (!prot.div) {
			prot.createDiv();
		}
		return prot.div;
	}
	pub.getDiv = getDiv;
	
	function close() {
		$(SignalPath).trigger('moduleBeforeClose', [ prot.jsonData, prot.div ])
		prot.div.remove();
		pub.onClose();
	}
	pub.close = close;
	
	pub.redraw = function() {}
	
	pub.clean = function() {
		pub.clearWarnings()
	}
	
	pub.onClose = function() {};
	
	function toJSON() {
		writePosition();
		if (prot.resizable) {
			prot.jsonData.layout.width = prot.div.css('width');
			prot.jsonData.layout.height = prot.div.css('height');	
		}
		return prot.jsonData;
	}
	pub.toJSON = toJSON;
	
	function addWarning(content) {
		var warning = $("<div class='warning ui-state-error'><span class='fa fa-exclamation'></span></div>");
		$(warning).click((function(msg) {
			return function() {
				alert(msg);
			}
		})(content));
		prot.warnings.push(warning)
		prot.div.append(warning);
	}
	pub.addWarning = addWarning

	function clearWarnings() {
		prot.warnings.forEach(function(warning) {
			warning.remove()
		})
		prot.warnings = []
	}
	pub.clearWarnings = clearWarnings
	
	function receiveResponse(payload) {}
	pub.receiveResponse = receiveResponse;

	function getUIChannelOptions() {
		// Check if module options contain channel options
		if (prot.jsonData.options && prot.jsonData.options.uiResendAll && prot.jsonData.options.uiResendAll.value) {
			return { resend_all: true }
		}
		else if (prot.jsonData.options && prot.jsonData.options.uiResendLast) {
			return { resend_last: prot.jsonData.options.uiResendLast.value }
		}
		else return { resend_all: true }
	}
	pub.getUIChannelOptions = getUIChannelOptions
	
	function updateFrom(data) {
		// Overwrite jsonData
		prot.jsonData = data;
		// But keep the hash
		prot.jsonData.hash = prot.hash;
		var classes = prot.div.attr('class')
		
		var top = prot.div.css("top");
		var left = prot.div.css("left");
		
		var oldCloseHandler = pub.onClose;
		pub.onClose = function() {};
		
		pub.close();
		
		// Recreate the div
		prot.createDiv()
		prot.div.css("top", top)
		prot.div.css("left", left)
		pub.onClose = oldCloseHandler
		prot.div.attr('class', classes)

		$prot.trigger('updated', data)
	}
	pub.updateFrom = updateFrom;
	
	function clone(callback) {
		var cloneData = jQuery.extend(true, {}, pub.toJSON());
		prot.prepareCloneData(cloneData);

		// Null hash value causes NumberFormatException in backend.
		delete cloneData["hash"]

		// Need new uiChannel
		delete cloneData["uiChannel"]


		// Re-fetch module data from server to ensure that uiChannels are regenerated
		var promise = $.ajax({
			type: 'POST',
			url: Streamr.createLink('module', 'jsonGetModule'),
			data: {
				id: cloneData.id,
				configuration: JSON.stringify(cloneData)
			},
			dataType: 'json',
		})

		promise.done(function(data) {
			var module = SignalPath.createModuleFromJSON(data)
			if (module) {
				callback(module)
			}
		})

		promise.fail(function(jqXHR, textStatus, errorThrown) {
			Streamr.showError("Error", errorThrown)
		})
	}
	prot.clone = clone;
	
	function prepareCloneData(cloneData) {
		// Set hash to null so that a new id will be assigned
		cloneData.hash = null;
		cloneData.layout.position.left = parseInt(cloneData.layout.position.left, 10) + 30 + 'px'
		cloneData.layout.position.top = parseInt(cloneData.layout.position.top, 10) + 30 + 'px'
	}
	prot.prepareCloneData = prepareCloneData;
	
	prot.onDrag = function() {}
	
	// Everything added to the public interface can be accessed from the
	// protected interface too
	$.extend(prot,pub);
	
	return pub;
}

if ($("#contextMenu").length==0)
	$("body").append('<ul id="contextMenu" class="dropdown-menu" role="menu" style="display:none" ></ul>')

$('body').contextMenu({
    selector: '#contextMenu',

    before: function(menu, target) {
		// Try to find an SP object in the hierarchy that has a context menu
		while (target.length && (!target.data('spObject') || !target.data('spObject').getContextMenu))
			target = target.parent();

		if (!target.length)
			return false;

		menu.data('spObject', target.data('spObject'))

		var prot = menu.data('spObject');
		var html = prot.getContextMenu(menu.data('target')).map(function(item) {
			return '<li><a tabindex="-1" data-cmd="' +
				item.cmd+'" href="#">' +
				item.title+'</a></li>';
		}).join('');
    	menu.html(html)
    },

    onSelected: function(menu, item) {
    	if (item.data('cmd') && menu.data('spObject'))
    		menu.data('spObject').handleContextMenuSelection(menu.data('spObject'), item.data('cmd'));
    }
});

// Remove module focus when clicking anywhere else on screen
$("body").click(function() {
	$(".component.focus").each(function(i,c) {
		$(c).data("spObject").removeFocus();
	});
});

// Modules are non-focused on load
$(SignalPath).on("loaded",function() {
	 $(".component").each(function(i,c) {
		 $(c).data("spObject").removeFocus();
	 });
});
