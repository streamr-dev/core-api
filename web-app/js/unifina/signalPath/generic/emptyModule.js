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
	var cpos = canvas.offset()
	var $prot = $(prot)

	prot.dragOptions = {
		drag: function(e, ui) {
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
			loadPosition(SignalPath.getWorkspace());
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

		helpLink.mouseenter(function() {
			var htext = prot.getHelp(false)
			// show tooltip after help text is loaded
			helpLink.tooltip({
				trigger: 'manual',
				container: '#canvas',
				viewport: {
					selector: '#'+prot.id,
					padding: 0
				},
				html: true,
				title: htext,
				placement: 'auto top',
				template: '<div class="tooltip" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner modulehelp-tooltip"></div></div>'
			})

			helpLink.tooltip('show')
		}).mouseleave(function() {
			helpLink.tooltip('hide')
		})

		helpLink.click(function() {
			bootbox.dialog({
				message: '<div class="modulehelp">'+ prot.getHelp(true)+'</div>',
				onEscape: function() { return true },
				animate: false,
				title: prot.jsonData.name
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
				
				// Create options
				for (var key in prot.jsonData.options) {
					if (prot.jsonData.options.hasOwnProperty(key)) {
						// Create the option div
						var div = prot.createOption(key, prot.jsonData.options[key]);
						$optionEditor.append(div);
						// Store reference to the JSON option
						$(div).data("option",prot.jsonData.options[key]);
					}
				}
				
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
		
		// Move modules on workspace change
		$(SignalPath).on("workspaceChanged", function(event, workspace, oldWorkspace) {
			writePosition(oldWorkspace)
			loadPosition(workspace, true)
		})

		$(SignalPath).trigger('moduleAdded', [ prot.jsonData, prot.div ])

		return prot.div;
	}
	prot.createDiv = createDiv;
	
	function writePosition(workspace) {
		prot.jsonData.layout = jQuery.extend(prot.jsonData.layout || {}, {
			position: {
				top: prot.div.css('top'),
				left: prot.div.css('left'),
			}
		});
		
		// Currently only save workspace position for dashboard modules
		if (prot.div.hasClass("dashboard")) {
			if (!prot.jsonData.layout.workspaces)
				prot.jsonData.layout.workspaces = {};
			
			if (workspace === "dashboard" && prot.div.css('top') == prot.jsonData.layout.workspaces.normal.top)
				console.log("Here we are!");
			
			prot.jsonData.layout.workspaces[workspace] = {
				position: {
					top: prot.div.css('top'),
					left: prot.div.css('left')
				}
			}
		}
	}
	prot.writePosition = writePosition;
	
	function loadPosition(workspace, animate) {
		var item = prot.jsonData.layout;
		
		// Width and height do not change in different workspaces
		if (item.width)
			prot.div.css('width',item.width);
		if (item.height)
			prot.div.css('height',item.height);
		
		// If workspace supplied then try to read position from there
		if (workspace && item.workspaces && item.workspaces[workspace])
			item = item.workspaces[workspace];
		
		// don't animate on transition to normal workspace, as jsPlumb won't keep up
		// TODO: maybe fix that some day
		if (animate && workspace=="dashboard") {
			prot.div.animate({
			    top: item.position.top,
			    left: item.position.left
			  }, 200);
		}
		else {
			prot.div.css('top',item.position.top);
			prot.div.css('left',item.position.left);
		}
	}
	prot.loadPosition = loadPosition;
	
	function setLayoutData(layout) {
		prot.jsonData.layout = layout;
		prot.loadPosition(SignalPath.getWorkspace(),false);
	}
	pub.setLayoutData = setLayoutData;
	
	function getLayoutData() {
		prot.writePosition(SignalPath.getWorkspace());
		return prot.jsonData.layout;
	}
	pub.getLayoutData = getLayoutData;
	
	function getHelp(extended) {
		var result = null;
    	$.ajax({
		    type: 'GET',
		    url: prot.signalPath.options.getModuleHelpUrl,
		    dataType: 'json',
		    success: function(data) {
		    	if (!data.helpText) {
		    		result = "No help is available for this module.";
		    	}
		    	else result = prot.renderHelp(data,extended);
			},
		    error: function() {
		    	result = "An error occurred while loading module help.";
		    },
		    data: {id:prot.jsonData.id},
		    async: false
		});
    	return result;
	}
	prot.getHelp = getHelp;
	
	function renderHelp(data) {
		var result = "<p>"+data.helpText+"</p>";
		return result;
	}
	prot.renderHelp = renderHelp;
	
	function initResizable(options) {
		var defaultOptions = {
			helper: "chart-resize-helper",
		}
		options = $.extend({},defaultOptions,options || {});
		prot.div.resizable(options);
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
		        {title: "Clone module", cmd: "clone"},
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
	 */
	function createOption(key, option) {
		var title
		var value
		var div = $("<div class='option'></div>");
		if (option.type=="int" || option.type=="double" || option.type=="string") {
			title = $("<span class='optionTitle'>"+key+"</span>");
			value = $("<span class='optionValue'></span>");

			div.append(title);
			div.append(value);

			var input = $("<input type='text'>");
			input.attr("value",option.value);
			value.append(input);
		}
		else if (option.type=="boolean") {
			title = $("<span class='optionTitle'>"+key+"</span>");
			value = $("<span class='optionValue'></span>");
			
			div.append(title);
			div.append(value);
			
			value.append("<select><option value='true' "+(option.value ? "selected='selected'" : "")+">true</option><option value='false' "+(!option.value ? "selected='selected'" : "")+">false</option></select>");
		}
		return div;
	}
	prot.createOption = createOption;
	
	function updateOption(option, div) {
		if (option.type=="int") {
			option.value = parseInt($(div).find("input").val());
		}
		else if (option.type=="double") {
			option.value = parseFloat($(div).find("input").val());
		}
		else if (option.type=="string") {
			option.value = $(div).find("input").val();
		}
		else if (option.type=="boolean") {
			option.value = ($(div).find("select").val()=="true");
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
		prot.div.remove();
		pub.onClose();
	}
	pub.close = close;
	
	pub.redraw = function() {}
	
	pub.clean = function() {}
	
	pub.onClose = function() {};
	
	function toJSON() {
		writePosition(SignalPath.getWorkspace());
		if (prot.resizable) {
			prot.jsonData.layout.width = prot.div.css('width');
			prot.jsonData.layout.height = prot.div.css('height');	
		}
		return prot.jsonData;
	}
	pub.toJSON = toJSON;
	
	function receiveResponse(payload) {
		if (payload.type=="warning") {
			var warning = $("<div class='warning ui-state-error'><span class='fa fa-exclamation'></span></div>");
			$(warning).click((function(msg) {
				return function() {
					alert(msg);
				}
			})(payload.msg));
			prot.div.append(warning);
		}
//		else console.log("EmptyModule.receiveResponse called, hash: "+prot.hash+", title: "+prot.jsonData.name+", message: "+JSON.stringify(payload));
	}
	pub.receiveResponse = receiveResponse;
	
	pub.endOfResponses = function() {}
	
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
	
	function clone() {
		var cloneData = jQuery.extend(true, {}, prot.toJSON());
		prot.prepareCloneData(cloneData);
		return SignalPath.createModuleFromJSON(cloneData);
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

$(SignalPath).on("workspaceChanged", function(event, name) {
	if (name=="dashboard") {
		// Hide components that do not have the dashboard class
		$(".component:not(.dashboard)").hide();
		$(".component.dashboard").addClass("dashboardEnabled");
	}
	else {
		$(".component").show();
		$(".component.dashboard").removeClass("dashboardEnabled");
	}
});
