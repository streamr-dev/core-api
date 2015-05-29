var renderChildren = function(ul, list){
	$.each(list, function(i, module){
		var li = $("<li/>")
		var a = $("<a/>", {
			text: module.data
		})
		li.append(a)
		ul.append(li)
		if(module.children){
			a.attr("href", "#category"+module.metadata.id)
			var innerUl = $("<ul/>", {
				class: "nav"
			})
			li.append(innerUl)
			renderChildren(innerUl, module.children)
		} else {
			a.attr("href", "#module"+module.metadata.id)
		}
	})
}   			
var renderSidebar = function(el, json){
	var el = $(el)
	var nav = $("<nav/>", {
		class: "bs-docs-sidebar"
	})
	var ul = $("<ul/>", {
		class: "nav bs-docs-sidenav",
		id: "module-help-browser"
	})
	el.append(nav)
	nav.append(ul)
	renderChildren(ul, json)
}

var level = 0
var renderModules = function(element,list){
	$.each(list, function(i, module){
		if(isModule(module)){
    		renderModule(element, module)
		} else {
			level++
			// var title = $("<h"+(level+1)+" id='"+module.metadata.id+"' style='padding-left:"+((level-1)*20)+"px;'>"+module.data+"</h"+(level+1)+">")
			var title = $("<h3 id='category"+module.metadata.id+"' style='padding-left:"+((level-1)*20)+"px;'>"+module.data+"</h3>")
			element.append(title)
			if(level == 0)
				max = module.children.length
			renderModules(element, module.children)
		}
		if(i == list.length-1) {
			level--
		}
	})
}
var renderModule = function(element, module){
	var panel = $("<div/>", {
		class: "panel",
		// style: "margin-left:"+(20*(level-1))+"px;",
		id: "module"+module.metadata.id
	})
	var panelHeading = $("<div/>", {
		class: "panel-heading"
	})
	var a = $("<a/>", {
		class: "accordion-toggle collapsed panel-heading",
		'data-toggle': "collapse",
		href: "#collapse"+module.metadata.id
	})
	var panelTitle = $("<span/>", {
		class: "",
		text: module.data
	})
	var collapse = $("<div/>", {
		id: "collapse"+module.metadata.id,
		class: "panel-collapse collapse"
	})
	var panelBody = $("<div/>", {
		class: "panel-body"
	})
	// a.append(panelHeading)
	a.append(panelTitle)
	panel.append(a)
	collapse.append(panelBody)
	panel.append(collapse)
	$.getJSON(moduleHelpUrl +"/"+ module.metadata.id, {}, function(moduleHelp){

		var helpText = $("<table class='table help-text-table'></table>")
		if(moduleHelp.helpText){
			// helpText.append($("<thead><tr><th>Help Text</th></tr></thead>"))
			helpText.append($("<tr><td><div class='help-text'></div></td></tr>"))
			helpText.find(".help-text").append($(moduleHelp.helpText))
		} else {
			helpText.append($("<thead><tr><th>No Help Text</th></tr></thead>"))
		}
		panelBody.append(helpText)

		var inputs = $("<table class='table input-table'></table>")
		if(moduleHelp.inputNames && moduleHelp.inputNames.length){
			inputs.append($("<thead><tr><th>Inputs</th><th>Name</th><th>Description</th></tr></thead>"))
			var tbody = $("<tbody></tbody>")
			$.each(moduleHelp.inputNames, function(i, inputName){
				inputs.append($("<tr><td></td><td>"+inputName+"</td><td>"+moduleHelp.inputs[inputName]+"</td></tr>"))
			})
		} else {
			inputs.append($("<thead><tr><th>No Inputs</th></tr></thead>"))
		}
		panelBody.append(inputs)

		var outputs = $("<table class='table output-table'></table>")
		if(moduleHelp.outputNames && moduleHelp.outputNames.length){
			outputs.append($("<thead><tr><th>Outputs</th><th>Name</th><th>Description</th></tr></thead>"))
			$.each(moduleHelp.outputNames, function(i, outputName){
				outputs.append($("<tr><td></td><td>"+outputName+"</td><td>"+moduleHelp.outputs[outputName]+"</td></tr>"))
			})
		} else {
			outputs.append($("<thead><tr><th>No Outputs</th></tr></thead>"))
		}
		panelBody.append(outputs)

		var params = $("<table class='table param-table'></table>")
		if(moduleHelp.paramNames && moduleHelp.paramNames.length){
			params.append($("<thead><tr><th>Parameters</th><th>Name</th><th>Description</th></tr></thead>"))
			var tbody = $("<tbody></tbody>")
			$.each(moduleHelp.paramNames, function(i, paramName){
				params.append($("<tr><td></td><td>"+paramName+"</td><td>"+moduleHelp.params[paramName]+"</td></tr>"))
			})
		} else {
			params.append($("<thead><tr><th>No Outputs</th></tr></thead>"))
		}
		panelBody.append(params)
	})
	element.append(panel)
}

var isModule = function(module){
	return !module.children
}
