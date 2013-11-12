package com.unifina.taglibs

class SignalPathTagLib {
	static namespace = "sp"

	private void writeScriptHeader(out) {
		out << '<script type="text/javascript">'
		out << '\$(document).ready(function() {'
	}

	private void writeScriptFooter(out) {
		out << '});'
		out << '</script>'
	}

	/**
	 * Renders a module search element.
	 *
	 * @attr id REQUIRED id and name of the input
	 * @attr controller defaults to "module"
	 * @attr action defaults to "jsonGetModules"
	 */
	def moduleSearch = {attrs, body->
		def id = attrs.id

		String url = g.createLink(controller:(attrs.controller ?: "module"), action:(attrs.action ?: "jsonGetModules"))

		out << "<input type='text' name='$id' id='$id'/>"
		
		writeScriptHeader(out)
		def str = """
					\$("#$id").autocomplete({
		              minLength: 2,
                      delay: 0,
		              select: function(event,ui) {
            			if (ui.item) {
				          SignalPath.addModule(ui.item.id,{});
                          \$('#$id').val("");
			            }
			            else {
				          // Nothing selected
			            }
		              }
	                }).data("autocomplete")._renderItem = function(ul,item) {
		              return \$("<li></li>")
		                .data("item.autocomplete",item)
		                .append("<a>"+item.name+"</a>")
		                .appendTo(ul);
	               };
		 """

		out << str
		out << getModuleUpdateString(url,id)
		writeScriptFooter(out)
	}

	private String getModuleUpdateString(String url, String id) {
		return """
		jQuery.getJSON("$url", function(data) {
			jQuery('#$id').autocomplete("option","source",(function(d) {
				return function(req,cb) {
					var result = []
					var term = req.term.toLowerCase();
					for (var i=0;i<data.length;i++) {
						if (data[i].name.toLowerCase().indexOf(req.term)!=-1)
							result.push(data[i]);
					}
					cb(result);
				};
			})(data));
		});
		 """
	}

	/**
	 * Renders a module browser element.
	 *
	 * @attr id REQUIRED id of the browser div
	 * @attr buttonId REQUIRED id of the "add module" button
	 * @attr controller defaults to "module"
	 * @attr action defaults to "jsonGetModuleTree"
	 */
	def moduleBrowser = {attrs, body->
		def id = attrs.id
		def buttonId = attrs.buttonId

		String url = g.createLink(controller:(attrs.controller ?: "module"), action:(attrs.action ?: "jsonGetModuleTree"))

		out << "<div id='$id'></div>"
		
		writeScriptHeader(out)
		def str = """
		jQuery("#$id").jstree({ 
			"core": {
				"animation": 100
			},
			
			"themes": {
				// If you change the theme, check app resources too
				theme: "classic",
				url: "${g.resource(dir:"js/jsTree/themes/classic", file:"style.css")}",
				"icons": false
			},
			
			"ui": {
				"select_limit": 1
			},
			
			"json_data" : {
				"ajax" : {
					"url" : "$url",
				}
			},
			
			"types": {
				"default" : {
				    draggable : false
				}
			},
			
			"plugins" : [ "json_data", "ui", "themes" ]
		})
		.bind("select_node.jstree", function (event, data) {
			// `data.rslt.obj` is the jquery extended node that was clicked
				if (data.rslt.obj.data("canAdd")) {
					jQuery('#$buttonId').button("enable");
				}
				else {
					jQuery('#$buttonId').button("disable");
					data.inst.open_node(data.rslt.obj);
				}
	    });
	    //.bind("loaded.jstree", function() {
		//	jQuery("#id ul").addClass("jstree-no-icons");
		//});
		// jQuery("#id").addClass("jstree-default jstree-no-icons");
		 """
		out << str
		writeScriptFooter(out)
		
	}
	
	/**
	 * Renders a module add button. The body of the element will become the button text.
	 *
	 * @attr buttonId REQUIRED id of the button
	 * @attr browserId REQUIRED id of the module browser
	 */
	def moduleAddButton = {attrs,body->
		def id = attrs.buttonId
		def browserId = attrs.browserId
		attrs.disabled = true
		
		out << button(attrs,body)
		
		writeScriptHeader(out)
		
		def str = """
	jQuery('#$id').click(function() {
		var id = jQuery('#$browserId').jstree("get_selected").data("id");
		SignalPath.addModule(id, {});
	});
		"""
		out << str
		
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a module tree refresh button. The body of the element will become the button text.
	 *
	 * @attr buttonId REQUIRED id of the button
	 * @attr browserId REQUIRED id of the module browser
	 */
	def moduleRefreshButton = {attrs,body->
		def id = attrs.buttonId
		def browserId = attrs.browserId
		
		out << button(attrs,body)
//		out << "<button id='$id'>${body()}</button>"
		
		writeScriptHeader(out)
		
		def str = """
	jQuery('#$id').click(function() {
		jQuery('#$browserId').jstree("refresh");
	});
		"""
		
		out << str
		
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a module run button that calls SignalPath.run(). The body of the element will become the button text.
	 *
	 * @attr buttonId REQUIRED id of the button
	 */
	def runButton = {attrs,body->
		def id = attrs.buttonId
		
//		out << "<button id='$id'>${body()}</button>"
		out << button(attrs,body)
		
		writeScriptHeader(out)
		
		def str = """
	jQuery('#$id').click(function() {
		SignalPath.run();
	});
		"""
		
		out << str
		
		writeScriptFooter(out)
	}
	
	/**
	 * Renders an abort button. The body of the element will become the button text.
	 *
	 * @attr buttonId REQUIRED id of the button
	 */
	def abortButton = {attrs,body->
		def id = attrs.buttonId
		
		attrs.disabled = true
		out << button(attrs,body)
		
		writeScriptHeader(out)
		
		def str = """
		jQuery('#$id').click(function() {
			SignalPath.abort();
		});
		jQuery(SignalPath).on('signalPathStart', function() {
			jQuery('#$id').button('enable');
		});
		jQuery(SignalPath).on('signalPathStop', function() {
			jQuery('#$id').button('disable');
		});
		"""
		
		out << str
		
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a button to add a specific module. The body of the element will become the button text.
	 *
	 * @attr buttonId REQUIRED id of the button
	 * @attr moduleId REQUIRED id of the module
	 */
	def addModuleShortcutButton = {attrs,body->
		def id = attrs.buttonId
		def moduleId = attrs.moduleId
		
		out << button(attrs,body)
//		out << "<button id='$id'>${body()}</button>"
		
		writeScriptHeader(out)
		
		def str = """
	jQuery('#$id').click(function() {
		SignalPath.addModule($moduleId,{});
	});
		"""
		
		out << str
		
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a spinner that will be shown when the signalpath is running.
	 *
	 * @attr id REQUIRED id of the spinner div
	 */
	def spinner = {attrs,body->
		def id = attrs.id
		
		out << "<span id='$id' style='display:none'>${body()}</span>"
		
		writeScriptHeader(out)
		def str = """
		jQuery(SignalPath).on('signalPathStart', function() {
			jQuery('#$id').show();
		});
		jQuery(SignalPath).on('signalPathStop', function() {
			jQuery('#$id').hide();
		});
		"""
		
		out << str
		
		writeScriptFooter(out)
	}
	
	/**
	 * @attr buttonId REQUIRED id of the button to be created
	 * @attr style CSS style for the button
	 */
	def button = {attrs, body->
		def buttonId = attrs.buttonId
		
//		String iconClass
//		if (body() && attrs.icon)
//			iconClass = "ui-button-text-icon-primary"
//		else if (body())
//			iconClass = "ui-button-text-only"
//		else if (attrs.icon)
//			iconClass = "ui-button-icon-only"
//	
//		out << "<button id='$buttonId' class='ui-button ui-widget ui-state-default ui-corner-all $iconClass ${attrs.class ?: ""} ${attrs.disabled ? "ui-state-disabled" : ""}' style='${attrs.style ?: ""}' ${attrs.title ? "title='attrs.title'" : ''} ${attrs.disabled ? "disabled='disabled'" : ""} role='button' aria-disabled='${attrs.disabled ? "true" : "false"}'>"
//		if (attrs.icon)
//			out << "<span class='ui-button-icon-primary ui-icon $attrs.icon'></span>"
//		if (body())
//			out << "<span class='ui-button-text'>${body()}</span>"
//		out << "</button>"
		
		out << "<button id='$buttonId' class='${attrs.class ?: ""} ${attrs.disabled ? "ui-state-disabled" : ""}' style='${attrs.style ?: ""}' ${attrs.title ? "title='attrs.title'" : ''} ${attrs.disabled ? "disabled='disabled'" : ""} role='button'>"
		out << body()
		out << "</button>"
	}
	
	/**
	 * Renders a button that will open a signalpath load browser
	 *
	 * @attr buttonId REQUIRED id of the button to be created
	 * @attr browserId REQUIRED id of the browser created with <sp:loadBrowser>
	 */
	def loadButton = {attrs,body->
		def buttonId = attrs.buttonId
		def browserId = attrs.browserId
		
		out << button(attrs,body)
		
		writeScriptHeader(out)
		def str = """
	jQuery("#$buttonId").click(function() {
		var lb = jQuery("#$browserId");
		if (lb.is(":visible")) {
			lb.hide();
			jQuery("#$browserId").tabs("destroy");
		}
		else {
			jQuery("#$browserId").tabs();
			lb.show();
			lb.position({my: "left top", at: "left bottom", of: "#$buttonId", collision:"none"});
		}
	});
		"""
		out << str
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a button that will open a signalpath load browser
	 *
	 * @attr id REQUIRED id of the browser
	 * @attr tabs a list of maps with keys: controller, action, params where the content of the tab must be available. The default values has one tab with controller: savedSignalPath, action: loadBrowser, and no params.
	 * @attr visible should the browser be instantly visible
	 */
	def loadBrowser = {attrs,body->
		def id = attrs.id
		def tabs = attrs.tabs ?: [[controller:"savedSignalPath",action:"loadBrowser",name:"Archive"]]
		def visible = attrs.visible ?: false
		def command = attrs.command ?: "SignalPath.loadSignalPath({url: url})"
		
		out << "<div id='$id'>"
		out << "<div class='loadTabs'>"
		out << "<ul>"
		tabs.each {
			// Inject the javascript command into params
			if (!it.params)
				it.params = [browserId:it.browserId,command:command]
			else if (it.params) {
				it.params.browserId = it.browserId
				it.params.command = command
			}
			
			out << "<li>"
			out << "<a href='${createLink(controller:it.controller,action:it.action,params:it.params)}'>$it.name</a>"
			out << "</li>"
		}
		out << "</ul>"
		out << "</div>"
		out << "</div>"
		
		writeScriptHeader(out)
		
		out << """
		if (typeof(SignalPath)!="undefined") {
			jQuery(SignalPath).on('signalPathLoad', function(event,saveData) {
				var lb = jQuery("#$id");
				if (lb.is(":visible")) {
					lb.hide();
					jQuery("#$id").tabs("destroy");
				}
			});
		}
		"""
		if (visible) {
			out << "jQuery('#$id').tabs().show();"
		}
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a button that will save the current signalpath.
	 *
	 * @attr buttonId REQUIRED id of the button to be created
	 */
	def saveButton = {attrs,body->
		def id = attrs.buttonId
		attrs.disabled = true
		out << button(attrs,body)
		
		writeScriptHeader(out)
		def str = """
			jQuery('#$id').click(function() {
				if (SignalPath.isSaved()) {
					SignalPath.saveSignalPath();
				}
				else alert('No savedata - use Save As');
			});

			jQuery(SignalPath).on('signalPathLoad', function(event,saveData) {
				jQuery('#$id').button("enable");
				jQuery('#$id .ui-button-text').html("Save to "+saveData.target);
			});

			jQuery(SignalPath).on('signalPathSave', function(event,saveData) {
				jQuery('#$id').button("enable");
				jQuery('#$id .ui-button-text').html("Save to "+saveData.target);
			});
		"""
		out << str
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a button that will open a save as dialog for the current signalpath.
	 *
	 * @attr buttonId REQUIRED id of the button to be created
	 * @attr dialogId REQUIRED id of the dialog to be created
	 * @attr url default: create url from controller and action names
	 * @attr controller default: "savedSignalPath"
	 * @attr action default: "save"
	 * @attr name the default name if the signalpath is not saved. default: empty string
	 * @attr targetName default: "Archive"
	 * @attr style default: ""
	 * @attr class default: ""
	 * @attr title default: ""
	 */
	def saveAsButton = {attrs,body->
		def id = attrs.buttonId
		def dialogId = attrs.dialogId
		def controller = attrs.controller ?: "savedSignalPath"
		def action = attrs.action ?: "save"
		def targetName = attrs.targetName ?: "Archive"
		def cls = attrs["class"] ?: ""
		def style = attrs.style ?: ""
		def title = attrs.title ?: ""
		
		out << button(attrs,body)
//		out << "<button id='$id' class='$cls' style='$style' title='$title'>${body()}</button>"

		def url = attrs.url ?: createLink(controller:controller,action:action)
		def dialogTitle = "Save to $targetName as.."
		def name = attrs.name ?: ""
		
		writeScriptHeader(out)
		def str = """
	jQuery('#$id').click(function() {
		if (jQuery('#$dialogId').length==0) {
			jQuery('body').append('<div id="$dialogId" style="display:none">Name: <input id="saveAsName" type="text"></div>');
		}

		jQuery('#saveAsName').val(SignalPath.isSaved() ? SignalPath.getSaveData().name : "$name");

		var callback = new function(saveData) {

		}

		jQuery('#$dialogId').dialog({
//			height: 100,
//			width: 350,
			modal: true,
			title: "$dialogTitle",
			buttons: {
				"Save": function() {
					var saveData = {
						url: "$url",
						target: "$targetName as new",
						name: jQuery('#saveAsName').val()
					};
				
					SignalPath.saveSignalPath(saveData, function(sd) {
						if (sd.showUrl)
							window.location = sd.showUrl;
					});

					jQuery(this).dialog("close");
					jQuery(this).dialog("destroy");
				},
				Cancel: function() {
					jQuery(this).dialog("close");
					jQuery(this).dialog("destroy");
				}
			}
		});


	});
		"""
		out << str
		writeScriptFooter(out)
	}
	
	/**
	 * Fetches SP parameters as JSON from given URL and renders them as a table.
	 *
	 * @attr url REQUIRED where to get the JSON from
	 * @attr parentId REQUIRED where in the DOM to add the table
	 * @attr id id of the table, default: 'parameterTable'
	 * @attr columns Column names in the table. Must have at least 2 columns! Default: ["Parameter","Value"]
	 * @attr populateColumnFunction If there are more than 2 columns, this javascript function name will be called with arguments (param,row,input)
	 * @attr onComplete javascript to run after the ajax request
	 */
	def paramTable = {attrs,body->
		def id = attrs.id ?: "parameterTable"
		def columns = attrs.columns ?: ["Parameter","Value"]
		
		writeScriptHeader(out)
		
		out << """
		jQuery.ajaxSettings.traditional = true;
		jQuery.getJSON("$attrs.url", function(data) {
		"""
		
		// Create the table
		out << """
			var table = jQuery("<table id='$id'><thead><tr></tr></thead><tbody></tbody></table>");
			jQuery("#$attrs.parentId").append(table);

			var inputs = jQuery([]);
			table.data("inputs",inputs);

			var tblHead = table.find("thead tr");
			var tblBody = table.find("tbody");
		"""
		
		// Create headers
		columns.each {
			out << "tblHead.append('<th>$it</th>');"
		}
		
		out << """
			jQuery(data.parameters).each(function(i,it) {
				var row = jQuery("<tr></tr>");
				tblBody.append(row);
				
				var name = jQuery("<td>"+(it.displayName!=null ? it.displayName : it.name)+"</td>");
				row.append(name);
				
				var inputTd = jQuery("<td></td>");
				row.append(inputTd);
				var input = SignalPath.getParamRenderer(it).create(null,it);
				inputTd.append(input);
				jQuery(input).trigger("spIOReady");
				input.data("parameterData",it);
				inputs.push(input);

				${attrs.populateColumnFunction ? "${attrs.populateColumnFunction}(it,row,input);" : ""}
			});

			${attrs.onComplete ? "${attrs.onComplete};" : ""}
		});
		"""
		
		writeScriptFooter(out)
	}
}
