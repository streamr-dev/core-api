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
		var jsTreeContainerOriginalHeight
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
				url: "${g.resource(dir:"js/jsTree/themes/classic", file:"style.css", plugin:"unifina-core")}",
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
		.bind('select_node.jstree', function (event, data) {
            if (jQuery("#$id").jstree('is_leaf', data.rslt.obj)) {
				jQuery('#$buttonId').attr('disabled', false)
			}
			else {
				jQuery('#$buttonId').attr('disabled', true)
				data.inst.open_node(data.rslt.obj)
			}
	    })
	    // SlimScroll is added because of the SlimScroll in the container. Otherwise the scrolling wouldn't work at all.
	    .bind('loaded.jstree', function() {
	    	jsTreeContainerOriginalHeight = jQuery(this).height()
			jQuery(this).slimscroll({
				wheelStep: 15,
				height: jsTreeContainerOriginalHeight
			})
		})
		.bind('close_node.jstree', function() {
			if (!jQuery("#$id").find('> ul > li.jstree-open').length) {
				jQuery("#$id").slimscroll({destroy: true})
				jQuery("#$id").slimscroll({
					wheelStep: 15,
					height: jsTreeContainerOriginalHeight
				})
			} else {
				jQuery(this).slimscroll()
			}
		})
	    .bind('open_node.jstree', function() {
			if (jQuery("#$id").find('> ul > li.jstree-open').length == 1) {
				jQuery("#$id").slimscroll({
					wheelStep: 15,
					height: 'auto'
				})
			} else {
				jQuery(this).slimscroll()
			}
		})
	    // This is a hack for the slimscroll, the scroll bar doesn't fade out on creation without the mouseover event
	    .mouseover()
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
	 * Renders a module run button that calls SignalPath.start(). The body of the element will become the button text.
	 *
	 * @attr buttonId REQUIRED id of the button
	 */
	def runButton = {attrs,body->
		def id = attrs.buttonId

		out << button(attrs,body)
		
		writeScriptHeader(out)
		
		def str = """
			var running = false

			function reset() {
				running = false
				jQuery('#$id').text('Run')
			}

			jQuery(SignalPath).on('error', reset)
			jQuery(SignalPath).on('stopped', reset)

			jQuery('#$id').click(function() {
				if (!running) {
					SignalPath.start()
					running = true
					jQuery('#$id').html('Abort <span class="fa fa-spin fa-spinner"></span>')
				}
				else
					SignalPath.stop()
			})
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
	 * @attr buttonId REQUIRED id of the button to be created
	 * @attr style CSS style for the button
	 */
	def button = {attrs, body->
		def buttonId = attrs.buttonId
		out << "<button id='$buttonId' class='btn ${attrs.class ?: "btn-default"}' style='${attrs.style ?: ""}' ${attrs.title ? "title='${attrs.title}'" : ''} ${attrs.disabled ? "disabled='disabled'" : ""} type='button'>"
		out << body()
		out << "</button>"
	}
		
	/**
	 * Renders a button dropdown that will show 'save/save as' links
	 */
	def saveButtonDropdown = {attrs,body->
		
		writeScriptHeader(out)
		def str = """
			\$('#saveButton').click(function() {
				if (SignalPath.isSaved()) {
					SignalPath.save()
				}
				else alert('Not saved - use Save As')
			})

			\$(SignalPath).on('loaded', function(event,savedJson) {
				if (SignalPath.isSaved()) {
					\$('#saveButton').parent().removeClass('disabled')
					\$('#saveButton').html('Save')
				}
				else {
					\$('#saveButton').parent().addClass('disabled')
				}
			})

			\$(SignalPath).on('new', function(event) {
				\$('#saveButton').parent().addClass('disabled')
			})

			\$(SignalPath).on('saved', function(event,savedJson) {
				\$('#saveButton').parent().removeClass('disabled')
				\$('#saveButton').html('Save')
			})

			// save as
			\$('#saveAsButton').click(function() {
				saveAsAndAskName()
			})

			
			\$('#save-dropdown').on('show.bs.dropdown', function () {
				var offset = \$('#save-dropdown-button').offset()
				\$('#save-dropdown-menu').css("top", offset.top + \$("#save-dropdown-button").outerHeight())
				\$('#save-dropdown-menu').css("left", offset.left)
				\$('#save-dropdown-menu').show()
			})
			\$('#save-dropdown').on('hide.bs.dropdown', function () {
				\$('#save-dropdown-menu').hide()
			})
		"""
		out << str
		writeScriptFooter(out)
		
		out << """
			<div id="save-dropdown" class="btn-group">
				<button id="save-dropdown-button" title="Save Canvas" type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
					<i class="fa fa-save"></i>
					<span class="caret"></span>
				</button>
			</div>
		"""

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
					SignalPath.save();
				}
				else alert('Not saved - use Save As');
			});

			jQuery(SignalPath).on('loaded', function(event, loadedData) {
				if (SignalPath.isSaved()) {
					jQuery('#$id').button("enable");
					jQuery('#$id .ui-button-text').html("Save");
				}
			});

			jQuery(SignalPath).on('saved', function(event, savedData) {
				jQuery('#$id').button("enable");
				jQuery('#$id .ui-button-text').html("Save");
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
