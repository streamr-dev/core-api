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

			crrm: {
				move: {
					check_move: function(m) {
						return false;
					}
				}
			},

			themes: {
				// If you change the theme, check app resources too
				theme: "classic",
				url: "${g.resource(dir:"js/jsTree/themes/classic", file:"style.css")}",
				"icons": false
			},
			
			ui: {
				'select_limit': 1
			},
			
			json_data : {
				ajax : {
					url : "$url",
				}
			},
			
			types: {
				default: {
				    draggable : false
				}
			},

			dnd: {
				copy: false,
				drop_target: '#container, #main-container, #canvas',
				drop_finish: function(drop) {
					console.log('drop finish', drop)
					SignalPath.addModule(drop.o.data('id'), {
						layout: {
							position: {
								top: drop.e.offsetY + 'px',
								left: drop.e.offsetX + 'px'
							}
						}
					})
				},
				drop_check: function(drop) {
		            return jQuery("#$id").jstree('is_leaf', drop.o)
				},
				drag_check: function(d) {
					return { after : false, before : false, inside : true };
				}
			},

			plugins : [ 'json_data', 'ui', 'themes', 'dnd', 'crrm' ]
		})
		.bind('dblclick.jstree', function(e, data) {
            var node = jQuery(e.target).closest("li")

            if (!jQuery("#$id").jstree('is_leaf', node))
            	return;

            var id = node.data('id')
			SignalPath.addModule(id, {})
		})
		.bind("select_node.jstree", function (event, data) {
            if (jQuery("#$id").jstree('is_leaf', data.rslt.obj)) {
				jQuery('#$buttonId').attr('disabled', false)
			}
			else {
				jQuery('#$buttonId').attr('disabled', true)
				data.inst.open_node(data.rslt.obj)
			}
	    })
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
		
		out << "<button id='$buttonId' class='btn ${attrs.class ?: "btn-default"}' style='${attrs.style ?: ""}' ${attrs.title ? "title='${attrs.title}'" : ''} ${attrs.disabled ? "disabled='disabled'" : ""} type='button'>"
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
			lb.modal('hide');
		}
		else {
			lb.modal();
		}
	});
		"""
		out << str
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a dropdown choice of workspace modes
	 * 
	 * @attr id REQURED id of the select
	 * @attr optionValues a list of values for the workspaces
	 * @attr optionNames a list of names corresponding to optionValues
	 */
	def workspaceDropdown = {attrs,body->
		def optionValues = attrs.optionValues ?: ['normal','dashboard']
		def optionNames = attrs.optionNames ?: ['Normal view','Dashboard view']
		
		out << "<select id='$attrs.id' style='${attrs.style ?: ""}'>"
		for (int i=0;i<optionValues.size();i++)
			out << "<option value='${optionValues[i]}'>${optionNames[i]}</option>"
		out << "</select>"
		
		writeScriptHeader(out)
		out << "jQuery('#${attrs.id}').change(function() { SignalPath.setWorkspace(jQuery(this).val()); });"
		out << "jQuery(SignalPath).on('signalPathWorkspaceChange', function(event, workspace) { jQuery('#${attrs.id}').val(workspace); });"
		writeScriptFooter(out)
	}
	
	/**
	 * Renders the signalpath load browser
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

		out << "<ul class='nav nav-tabs' role='tablist'>"
 
		// nav tabs
		tabs.each {
			// Inject the javascript command into params
			if (!it.params)
				it.params = [browserId:it.browserId, command:command]
			else if (it.params) {
				it.params.browserId = it.browserId
				it.params.command = command
			}

			out << "<li><a data-target='#loadBrowser_${it.browserId}' \
						data-url='${createLink(controller:it.controller, action:it.action, params:it.params)}' \
						data-toggle='tab' \
						role='tab'>${it.name}\
					</a></li>"
		}

		out << '</ul>'

		// tab panes
		out << '<div class="tab-content">'
		tabs.each {
			out << "<div class=\"tab-pane\" id=\"loadBrowser_${it.browserId}\"></div>"
		}
		out << '</div>'

		writeScriptHeader(out) 

		out << """
			jQuery('#$id a[data-toggle="tab"]').on('show.bs.tab', function (e) {
				jQuery.get(jQuery(e.target).data('url'), function(content) {
					jQuery(jQuery(e.target).data('target')).html(content);
				});
			});

			jQuery('#$id a[data-toggle="tab"]:first').tab('show');

			if (typeof(SignalPath)!=='undefined') {
		 		jQuery(SignalPath).on('signalPathLoad', function(event, saveData) {
		 			var lb = jQuery('#$id');
		 			if (lb.is(':visible'))
						lb.modal('hide');
			 	});
			 }
		 """

		 writeScriptFooter(out)
	}

	/**
	 * Renders a button dropdown that will show 'save/save as' links
	 */
	def saveButtonDropdown = {attrs,body->
		out << """
			<div class="btn-group">
				<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
					<span class="fa fa-save"></span>
					<span class="caret"></span>
				</button>

				<ul class="dropdown-menu" role="menu">
					<li class="disabled"><a href="#" id="saveButton">Save</a></li>
					<li><a href="#" id="saveAsButton">Save as...</a></li>
				</ul>
			</div>
		"""

		writeScriptHeader(out)
		def str = """
			\$('#saveButton').click(function() {
				if (SignalPath.isSaved()) {
					SignalPath.saveSignalPath()
				}
				else alert('No savedata - use Save As')
			})

			\$(SignalPath).on('signalPathLoad', function(event,saveData) {
				if (saveData.isSaved) {
					\$('#saveButton').parent().removeClass('disabled')
					\$('#saveButton').html('Save to '+saveData.target)
				}
			})

			\$(SignalPath).on('signalPathSave', function(event,saveData) {
				\$('#saveButton').parent().removeClass('disabled')
				\$('#saveButton').html('Save to '+saveData.target)
			})

			// save as
			\$('#saveAsButton').click(function() {
				bootbox.prompt('Save to Archive as..', function(saveAsName) {
					if (!saveAsName)
						return;

					var saveData = {
						url: '${ createLink(controller: "savedSignalPath", action: "save") }',
						target: "Archive as new",
						name: saveAsName
					}
				
					SignalPath.saveSignalPath(saveData, function(sd) {
						if (sd.showUrl)
							window.location = sd.showUrl
					})
				})
			})
		"""
		out << str
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
				if (saveData.isSaved) {
					jQuery('#$id').button("enable");
					jQuery('#$id .ui-button-text').html("Save to "+saveData.target);
				}
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

		def url = attrs.url ?: createLink(controller:controller,action:action)
		def dialogTitle = "Save to $targetName as.."
		def name = attrs.name ?: ""
		
		writeScriptHeader(out)
		def str = """
			jQuery('#$id').click(function() {
				bootbox.prompt('$dialogTitle', function(saveAsName) {
					if (!saveAsName)
						return;

					var saveData = {
						url: "$url",
						target: "$targetName as new",
						name: saveAsName
					};
				
					SignalPath.saveSignalPath(saveData, function(sd) {
						if (sd.showUrl)
							window.location = sd.showUrl;
					});

					jQuery(this).dialog("close");
					jQuery(this).dialog("destroy");
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
			var table = jQuery("<table id='$id' class='table table-hover table-condensed'><thead><tr></tr></thead><tbody></tbody></table>");
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
				
				var name = jQuery("<td class='col-sm-3'>"+(it.displayName!=null ? it.displayName : it.name)+"</td>");
				row.append(name);
				
				var inputTd = jQuery("<td class='col-sm-3'></td>");
				row.append(inputTd);
				var input = SignalPath.getParamRenderer(it).create(null,it);
				inputTd.append(input);
				input.addClass("form-control")
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
