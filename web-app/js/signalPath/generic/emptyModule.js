SignalPath.EmptyModule = function(data, canvas, my) {
	my = my || {};

	my.div = null;
	my.header = null;
	my.title = null;
	my.body = null;
	my.resizable = false;
	
	my.jsonData = data;
	my.hash = my.jsonData.hash;
	my.type = my.jsonData.type;
	my.id = "module_"+my.hash;

	var that = {};
	
	my.dragOptions = {
		drag: function() {
			my.onDrag();
		},
		//handle: '.moduleheader'
	}
	
	that.getDragOptions = function() {
		return my.dragOptions;
	}
	
	function createDiv() {
		my.div = $("<div id='"+my.id+"' class='component context-menu "+my.type+"'></div>");
		my.div.data("spObject",my);
		
		// Set absolute position
		my.div.css("position","absolute");
		
		// Read position and width/height if saved
		if (my.jsonData.layout) {
			loadPosition(SignalPath.getWorkspace());
		} 
		// Else add to default position in viewport
		else {
			my.div.css('top',$(window).scrollTop());
			my.div.css('left',$(window).scrollLeft());
		}
		
		// Module header
		my.header = $("<div class='moduleheader'></div>");
		my.div.append(my.header);
		
		// Create title
		my.title = $("<span class='modulename'>"+my.jsonData.name+"</span>");
		my.header.append(my.title);
	
//		var deleteLink = $("<span class='delete modulebutton ui-corner-all ui-icon ui-icon-closethick'></span>");
		var deleteLink = createModuleButton("delete ui-icon ui-icon-closethick")
	
		deleteLink.click(function() {
			that.close();
		});
		my.header.append(deleteLink);
	
//		module.append("<br/>");
	
		my.body = $("<div class='modulebody'></div>");
		my.div.append(my.body);
		
		// If there are options, create options editor
		if (my.jsonData.options != null) {
			my.optionEditor = $("<div class='optionEditor'></div>");
			my.body.append(my.optionEditor);
			
			// Create options
			for (var key in my.jsonData.options) {
				if (my.jsonData.options.hasOwnProperty(key)) {
					// Create the option div
					var div = my.createOption(key, my.jsonData.options[key]);
					my.optionEditor.append(div);
					// Store reference to the JSON option
					$(div).data("option",my.jsonData.options[key]);
				}
			}
			
			my.optionEditor.dialog({
				autoOpen: false,
				title: "Options: "+my.title.text(),
				buttons: [{text: "Ok", click: function() {
						$(this).dialog("close"); 
						$(my.optionEditor).find(".option").each(function(i,div) {
							// Get reference to the JSON option
							my.updateOption($(div).data("option"), div);
						});
						
						my.onOptionsUpdated();
					}},
				    {text: "Cancel", click: function() {
				    	$(this).dialog("close");
				    }}]
			});
			
//			var editOptions = $("<span class='options modulebutton ui-corner-all ui-icon ui-icon-wrench'></span>");
			var editOptions = createModuleButton("options ui-icon ui-icon-wrench");
			
			editOptions.click(function() {
				// Location
				my.optionEditor.dialog("open");
			});
			my.header.append(editOptions);
		}
		
		if (my.jsonData.canRefresh) {
			// If the module can refresh, add a refresh button
//			var refresh = $("<span class='refresh modulebutton ui-corner-all ui-icon ui-icon-refresh'></span>");
			var refresh = createModuleButton("refresh ui-icon ui-icon-refresh");
		
			refresh.click(function() {
				SignalPath.updateModule(that);
			});
			my.header.append(refresh);
		}
		
		// Must add to canvas before setting draggable
		canvas.append(my.div);
		my.div.draggable(my.dragOptions);
		
		my.div.on('spContextMenuSelection', (function(d,j) {
			return function(event,selection) {
				my.handleContextMenuSelection(d,j,selection,event);
			};
		})(my.div,my.jsonData));
		
		my.div.on("click dragstart", function(event) {
			$(".component.focus").each(function(i,c) {
				var ob = $(c).data("spObject");
				if (ob != my)
					ob.removeFocus();
			});
			my.addFocus(true);
			event.stopPropagation();
		});
		
		my.div.hover(function() {
			if (!my.div.hasClass("focus")) {
				my.addFocus(false);
			}
		}, function() {
			if (!my.div.hasClass("holdFocus")) {
				my.removeFocus();
			}
		});
		
		// A module is focused by default when it is created
		my.addFocus(true);
		
		// Move modules on workspace change
		$(SignalPath).on("signalPathWorkspaceChange", function(event, workspace, oldWorkspace) {
			writePosition(oldWorkspace);
			loadPosition(workspace, true);
		});
		
		return my.div;
	}
	my.createDiv = createDiv;
	
	function writePosition(workspace) {
		my.jsonData.layout = jQuery.extend(my.jsonData.layout || {}, {
			position: {
				top: $(my.div).css('top'),
				left: $(my.div).css('left'),
			}
		});
		
		// Currently only save workspace position for dashboard modules
		if (my.div.hasClass("dashboard")) {
			if (my.jsonData.layout.workspaces==null)
				my.jsonData.layout.workspaces = {};
			
			if (workspace=="dashboard" && $(my.div).css('top')==my.jsonData.layout.workspaces.normal.top)
				console.log("Here we are!");
			
			my.jsonData.layout.workspaces[workspace] = {
				position: {
					top: $(my.div).css('top'),
					left: $(my.div).css('left')
				}
			}
		}
	}
	my.writePosition = writePosition;
	
	function loadPosition(workspace, animate) {
		var item = my.jsonData.layout;
		
		// Width and height do not change in different workspaces
		if (item.width)
			my.div.css('width',item.width);
		if (item.height)
			my.div.css('height',item.height);
		
		// If workspace supplied then try to read position from there
		if (workspace!=null && item.workspaces!=null && item.workspaces[workspace]!=null)
			item = item.workspaces[workspace];
		
		// don't animate on transition to normal workspace, as jsPlumb won't keep up
		// TODO: maybe fix that some day
		if (animate && workspace=="dashboard") {
			my.div.animate({
			    top: item.position.top,
			    left: item.position.left
			  }, 200);
		}
		else {
			my.div.css('top',item.position.top);
			my.div.css('left',item.position.left);
		}
	}
	my.loadPosition = loadPosition;
	
	function initResizable(options) {
		var defaultOptions = {
			helper: "chart-resize-helper",
		}
		options = $.extend({},defaultOptions,options || {});
		my.div.resizable(options);
		my.resizable = true;
	}
	my.initResizable = initResizable;
	
	function removeFocus() {
		$(my.div).removeClass("focus");
		$(my.div).removeClass("hoverFocus");
		$(my.div).removeClass("holdFocus");
		$(my.div).find(".showOnFocus").fadeTo(100,0);
	}
	my.removeFocus = removeFocus;
	
	function addFocus(hold) {
		$(my.div).addClass("focus");
		
		if (hold) $(my.div).addClass("holdFocus");
		else $(my.div).addClass("hoverFocus");
		
		$(my.div).find(".showOnFocus").fadeTo(100,1);
	}
	my.addFocus = addFocus;
	
	function createModuleButton(additionalClasses) {
		var button = $("<span class='modulebutton ui-corner-all ui-state-default showOnFocus "+(additionalClasses ? additionalClasses : "")+"'></span>");
		button.hover(function() {$(this).addClass("ui-state-highlight");}, function() {$(this).removeClass("ui-state-highlight")});
		return button;
	}
	my.createModuleButton = createModuleButton;
	
	function getContextMenu(div) {
		return [
		        {title: "Clone module", cmd: "clone"},
		]
	}
	my.getContextMenu = getContextMenu;
	
	function handleContextMenuSelection(div,data,selection,event) {
		if (selection=="clone") {
			my.clone();
			event.stopPropagation();
		}
	}
	my.handleContextMenuSelection = handleContextMenuSelection;
	
	/**
	 * Functions for rendering and parsing options
	 */
	function createOption(key, option) {
		var div = $("<div class='option'></div>");
		if (option.type=="int" || option.type=="double" || option.type=="string") {
			var title = $("<span class='optionTitle'>"+key+"</span>");
			var value = $("<span class='optionValue'></span>");
			
			div.append(title);
			div.append(value);
			
			var input = $("<input type='text'>");
			input.attr("value",option.value);
			value.append(input);
		}
		else if (option.type=="boolean") {
			var title = $("<span class='optionTitle'>"+key+"</span>");
			var value = $("<span class='optionValue'></span>");
			
			div.append(title);
			div.append(value);
			
			value.append("<select><option value='true' "+(option.value ? "selected='selected'" : "")+">true</option><option value='false' "+(!option.value ? "selected='selected'" : "")+">false</option></select>");
		}
		return div;
	}
	my.createOption = createOption;
	
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
	my.updateOption = updateOption;
	
	/**
	 * Called when options are updated
	 */
	function onOptionsUpdated() {
		// Reload the module
		SignalPath.updateModule(that);
	}
	my.onOptionsUpdated = onOptionsUpdated;
	
	function getHash() {
		return my.hash;
	}
	that.getHash = getHash;
	
	function getModuleId() {
		return my.jsonData.id;
	}
	my.getModuleId = getModuleId;
	
	function getDiv() {
		if (my.div==null) {
			my.createDiv();
		}
		return my.div;
	}
	that.getDiv = getDiv;
	
//	that.getType = function() {
//		return my.type;
//	}
	
	function close() {
		$(my.div).remove();
		that.onClose();
	}
	that.close = close;
	
	that.redraw = function() {}
	
	that.clean = function() {}
	
	that.onClose = function() {};
	
	function toJSON() {
		writePosition(SignalPath.getWorkspace());
		if (my.resizable) {
			my.jsonData.layout.width = $(my.div).css('width');
			my.jsonData.layout.height = $(my.div).css('height');	
		}
		return my.jsonData;
	}
	that.toJSON = toJSON;
	
	function receiveResponse(payload) {
		if (payload.type=="warning") {
			var warning = $("<div class='warning ui-state-error'><span class='ui-corner-all ui-icon ui-icon-alert'></span></div>");
			$(warning).click((function(msg) {
				return function() {
					alert(msg);
				}
			})(payload.msg));
			my.div.append(warning);
		}
//		else console.log("EmptyModule.receiveResponse called, hash: "+my.hash+", title: "+my.jsonData.name+", message: "+JSON.stringify(payload));
	}
	that.receiveResponse = receiveResponse;
	
	that.endOfResponses = function() {}
	
	function updateFrom(data) {
		// Overwrite jsonData
		my.jsonData = data;
		// But keep the hash
		my.jsonData.hash = my.hash;
		
		// Recreate module
		var top = $(my.div).css("top");
		var left = $(my.div).css("left");
		
		// Kill close handlers
		var oldCloseHandler = that.onClose;
		that.onClose = function() {};
		
		that.close();
		
		// Recreate the div
		my.createDiv();
		
		// Reposition the div
		$(my.div).css("top",top);
		$(my.div).css("left",left);
		
		// Reset the close handlers
		that.onClose = oldCloseHandler;
	}
	that.updateFrom = updateFrom;
	
	function clone() {
		// Deep copy my data
		var cloneData = jQuery.extend(true, {}, my.toJSON());
		my.prepareCloneData(cloneData);
		return SignalPath.createModuleFromJSON(cloneData);
	}
	my.clone = clone;
	
	function prepareCloneData(cloneData) {
		// Set hash to null so that a new id will be assigned
		cloneData.hash = null;
	}
	my.prepareCloneData = prepareCloneData;
	
	my.onDrag = function() {}
	
	// Everything added to the public interface can be accessed from the
	// private interface too
	$.extend(my,that);
	
	return that;
}

$(document).contextmenu({
    delegate: ".context-menu",
    menu: [],
    show: 100,
    select: function(event, ui) {
    	var target = $(document).data("contextMenuTarget"); 
    	if (ui.cmd) {
    		target.trigger("spContextMenuSelection",ui.cmd);
    	}
    },
	beforeOpen: function(event, ui) {
		var target = $(ui.target);
		
		// Try to find an SP object in the hierarchy that has a context menu
		while (target!=null && (target.data("spObject")==null || target.data("spObject").getContextMenu==null))
			target = target.parent();
			
		if (target!=null) {
			var my = target.data("spObject");
			
			// This hack is a workaround: somehow the contextmenu plugin loses ui.target before calling select()
			$(document).data("contextMenuTarget",target);
			$(document).contextmenu("replaceMenu", my.getContextMenu(ui.target));
		}
	}
});

// Remove module focus when clicking anywhere else on screen
$("body").click(function() {
	$(".component.focus").each(function(i,c) {
		$(c).data("spObject").removeFocus();
	});
});

// Modules are non-focused on load
$(SignalPath).on("signalPathLoad",function() {
	 $(".component").each(function(i,c) {
		 $(c).data("spObject").removeFocus();
	 });
});

$(SignalPath).on("signalPathWorkspaceChange", function(event, name, old) {
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