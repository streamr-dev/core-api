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
					jQuery('#$buttonId').removeAttr("disabled");
				}
				else {
					jQuery('#$buttonId').attr("disabled","disabled");
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
	 * @attr id REQUIRED id of the button
	 * @attr browserId REQUIRED id of the module browser
	 */
	def moduleAddButton = {attrs,body->
		def id = attrs.id
		def browserId = attrs.browserId
		
		out << "<button id='$id' disabled='disabled'>${body()}</button>"
		
		writeScriptHeader(out)
		
		def str = """
	jQuery('#$id').click(function() {
		var id = jQuery('#$browserId').jstree("get_selected").data("id");
		SignalPath.addModule(id, {});
	}).attr("disabled","disabled");
		"""
		out << str
		
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a module tree refresh button. The body of the element will become the button text.
	 *
	 * @attr id REQUIRED id of the button
	 * @attr browserId REQUIRED id of the module browser
	 */
	def moduleRefreshButton = {attrs,body->
		def id = attrs.id
		def browserId = attrs.browserId
		
		out << "<button id='$id'>${body()}</button>"
		
		writeScriptHeader(out)
		
		def str = """
	jQuery('#$id').click(function() {
		jQuery('#browserId').jstree("refresh");
	});
		"""
		
		out << str
		
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a module run button that calls SignalPath.run(). The body of the element will become the button text.
	 *
	 * @attr id REQUIRED id of the button
	 */
	def runButton = {attrs,body->
		def id = attrs.id
		
		out << "<button id='$id'>${body()}</button>"
		
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
	 * @attr id REQUIRED id of the button
	 */
	def abortButton = {attrs,body->
		def id = attrs.id
		
		out << "<button id='$id' disabled='disabled'>${body()}</button>"
		
		writeScriptHeader(out)
		
		def str = """
		jQuery('#$id').click(function() {
			SignalPath.abort();
		});
		jQuery(SignalPath).on('signalPathStart', function() {
			jQuery('#$id').removeAttr('disabled');
		});
		jQuery(SignalPath).on('signalPathStop', function() {
			jQuery('#$id').attr('disabled','disabled');
		});
		"""
		
		out << str
		
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a button to add a specific module. The body of the element will become the button text.
	 *
	 * @attr id REQUIRED id of the button
	 * @attr moduleId REQUIRED id of the module
	 */
	def addModuleShortcutButton = {attrs,body->
		def id = attrs.id
		def moduleId = attrs.moduleId
		
		out << "<button id='$id'>${body()}</button>"
		
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
	 * @attr id REQUIRED id of the button
	 */
	def spinner = {attrs,body->
		def id = attrs.id
		
		out << "<div id='$id' class='spinner' style='display:none'>${body()}</div>"
		
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
	 * Renders a button that will open a signalpath load browser
	 *
	 * @attr buttonId REQUIRED id of the button to be created
	 * @attr browserId REQUIRED id of the browser created with <sp:loadBrowser>
	 */
	def loadButton = {attrs,body->
		def buttonId = attrs.buttonId
		def browserId = attrs.browserId
		
		out << "<button id='$buttonId'>${body()}</button>"
		
		writeScriptHeader(out)
		def str = """
	jQuery("#$buttonId").click(function() {
		var lb = jQuery("#$browserId");
		if (lb.is(":visible")) {
			lb.hide();
			jQuery("#$browserId .loadTabs").tabs("destroy");
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
	 */
	def loadBrowser = {attrs,body->
		def id = attrs.id
		def tabs = attrs.tabs ?: [[controller:"savedSignalPath",action:"loadBrowser"]]
		
		out << "<div id='$id'>"
		out << "<div class='loadTabs'>"
		out << "<ul>"
		tabs.each {
			out << "<li>"
			out << "<a href='${createLink(controller:it.controller,action:it.action,params:it.params)}'>$it.name</a>"
			out << "</li>"
		}
		out << "</ul>"
		out << "</div>"
		out << "</div>"
	}
	
	/**
	 * Renders a button that will save the current signalpath.
	 *
	 * @attr id REQUIRED id of the button to be created
	 */
	def saveButton = {attrs,body->
		def id = attrs.id
		
		out << "<button id='$id' disabled='disabled'>${body()}</button>"
		
		writeScriptHeader(out)
		def str = """
			jQuery('#$id').click(function() {
				if (SignalPath.isSaved()) {
					SignalPath.saveSignalPath();
				}
				else alert('No savedata - use Save As');
			});

			jQuery('SignalPath').on('signalPathLoad signalPathSave', function() {
				if (SignalPath.isSaved())
					jQuery('#$id').removeAttr('disabled');
			});

		"""
		out << str
		writeScriptFooter(out)
	}
	
	/**
	 * Renders a button that will open a save as dialog for the current signalpath.
	 *
	 * @attr id REQUIRED id of the button to be created
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
		def id = attrs.id
		def dialogId = attrs.dialogId
		def controller = attrs.controller ?: "savedSignalPath"
		def action = attrs.action ?: "save"
		def targetName = attrs.targetName ?: "Archive"
		def cls = attrs["class"] ?: ""
		def style = attrs.style ?: ""
		def title = attrs.title ?: ""
		
		out << "<button id='$id' class='$cls' style='$style' title='$title'>${body()}</button>"

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
}
