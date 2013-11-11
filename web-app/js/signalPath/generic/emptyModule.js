SignalPath.EmptyModule = function(data, canvas, my) {
	my = my || {};

	my.div = null;
	my.header = null;
	my.title = null;
	my.body = null;
	
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
		my.div = $("<div id='"+my.id+"' class='component "+my.type+"'></div>");
		my.div.data("me",my);
		
		// Set absolute position
		my.div.css("position","absolute");
		
		// Read position and width/height if saved
		if (my.jsonData.layout) {
			my.div.css('top',my.jsonData.layout.position.top);
			my.div.css('left',my.jsonData.layout.position.left);
			
			if (my.jsonData.layout.width)
				my.div.css('width',my.jsonData.layout.width);
			if (my.jsonData.layout.height)
				my.div.css('height',my.jsonData.layout.height);
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
		
		return my.div;
	}
	my.createDiv = createDiv;
	
	function createModuleButton(additionalClasses) {
		var button = $("<span class='modulebutton ui-corner-all ui-state-default "+(additionalClasses ? additionalClasses : "")+"'></span>");
		button.hover(function() {$(this).addClass("ui-state-highlight");}, function() {$(this).removeClass("ui-state-highlight")});
		return button;
	}
	my.createModuleButton = createModuleButton;
	
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
			
			value.append("<input type='text' value='"+option.value+"'>");
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
		my.jsonData.layout = jQuery.extend(my.jsonData.layout || {}, {
			position: {
				top: $(my.div).css('top'),
				left: $(my.div).css('left')
			}
		});
		
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
	
	
	my.onDrag = function() {}
	
	return that;
}