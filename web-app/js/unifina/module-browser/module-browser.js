(function(exports) {

function ModuleBrowser(options){
	var _this = this

	this.url = options.url
	this.sidebarEl = $(options.sidebarEl)
	this.moduleTreeEl = $(options.moduleTreeEl)
	this.searchBoxEl = $(options.searchBoxEl)

	this.level = 0
	
	$.getJSON(this.url +"/jsonGetModuleTree/", {}, function(moduleTree){
		var sidebar = new Sidebar(_this.sidebarEl, moduleTree)
		_this.renderModules(_this.moduleTreeEl, moduleTree)
		_this.offset = 80
		sidebar.initSearch(_this.searchBoxEl, _this.offset)

		$('body').scrollspy({
			offset: _this.offset
		})
		$('#sidebar li a').click(function(event) {
			event.preventDefault()
		    $($(this).attr('href'))[0].scrollIntoView()
		    scrollBy(0, -(_this.offset-30))
	        this.blur()
		});
	})
}
ModuleBrowser.prototype.renderModules = function(element,list){
	var _this = this
	
	$.each(list, function(i, module){
		if(!module.children){
    		new Module(_this.url, element, module, _this.level)
		} else {
			_this.level++
			if(_this.level == 1)
				var h = "h3"
			else
				var h = "h4"
			// var title = $("<h"+(level+1)+" id='"+module.metadata.id+"' style='padding-left:"+((level-1)*20)+"px;'>"+module.data+"</h"+(level+1)+">")
			var title = $("<"+h+" id='category"+module.metadata.id+"' style='padding-left:"+((_this.level-1)*20)+"px;'>"+module.data+"</"+h+">")
			element.append(title)
			_this.renderModules(element, module.children)
		}
		if(i == list.length-1) {
			_this.level--
		}
	})
}
function Sidebar(el, moduleTree){
	this.modules = {}
	this.renderSidebar(el, moduleTree)
}
// The searchbox now searches from the modules and categorys by the input and 
// scrolls to the next search result. If a search with a space is typed, it first looks that is
// there any results by the whole search (e.g. 'time series' > 'Time Series') and then by the last word
// (e.g. 'time statistics corr' > 'Correlation')
Sidebar.prototype.initSearch = function(searchBoxEl, offset){
	var _this = this
	this.offset = offset
	this.searchBox = $(searchBoxEl)
	var moduleNames = Object.keys(this.modules)
	this.msgField = $(".search-message")

	var scroll = function(href){
		var module = $(href)
		if(module.length){
			module[0].scrollIntoView()
			window.scrollBy(0, -(_this.offset-30))
		}
	}

	var lastSearch = null
	var index = 0
	var search = function(text){
		if(lastSearch == text)
			index++
		else {
			index = 0
			lastSearch = text
		}
		if(text == "")
			return null
		else {
			var names = _.filter(moduleNames, function(str){
				return str.lastIndexOf(text, 0) === 0
			})
			if(names.length < index+1){
				index = 0
				return _this.modules[names[0]]
			}
			else 
				return _this.modules[names[index]]
		}
	}

	this.searchBox.on("keyup", function(e){
		var s = _this.searchBox.val().toLowerCase()
		if(s){
			var href = search(s)
			if(href === undefined){
				var words = s.split(" ")
				href = search(words[words.length-1])
			}
			if(href === undefined){
				_this.msgField.html("No search results")
			} else {
				_this.msgField.empty()
			}
			scroll(href)
		} else {
			$("body").scrollTop(0)
			_this.msgField.empty()
		}
	})
}
Sidebar.prototype.renderChildren = function(ul, list){
	var _this = this
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
			_this.modules[module.data.toLowerCase()] = "#category"+module.metadata.id
			_this.renderChildren(innerUl, module.children)
		} else {
			a.attr("href", "#module"+module.metadata.id)
			_this.modules[module.data.toLowerCase()] = "#module"+module.metadata.id
		}
	})
}   			
Sidebar.prototype.renderSidebar = function(el, json){
	var el = $(el)
	var nav = $("<nav/>", {
		class: "streamr-sidebar"
	})
	var ul = $("<ul/>", {
		class: "nav",
		id: "module-help-browser"
	})
	el.append(nav)
	nav.append(ul)
	this.renderChildren(ul, json)
}
function Module(url, element, module, level){
	this.level = level
	this.element = element
	this.module = module
	this.url = url
	this.render()
}
Module.prototype.render = function(){
	var _this = this
	this.panel = $("<div/>", {
		class: "panel",
		style: "margin-left:"+(20*(_this.level-1))+"px;",
		id: "module"+this.module.metadata.id
	})
	this.panelHeading = $("<div/>", {
		class: "panel-heading"
	})
	this.a = $("<a/>", {
		class: "accordion-toggle collapsed panel-heading",
		'data-toggle': "collapse",
		href: "#collapse"+this.module.metadata.id
	})
	this.panelTitle = $("<span/>", {
		class: "",
		text: this.module.data
	})
	this.collapse = $("<div/>", {
		id: "collapse"+this.module.metadata.id,
		class: "panel-collapse collapse"
	})
	this.panelBody = $("<div/>", {
		class: "panel-body"
	})
	// a.append(panelHeading)
	this.a.append(this.panelTitle)
	this.panel.append(this.a)
	this.collapse.append(this.panelBody)
	this.panel.append(this.collapse)
	
	_this.renderHelp()
	

	this.element.append(this.panel)
}
Module.prototype.renderHelp = function(msg){
	var _this = this

	$.getJSON(this.url +"/jsonGetModuleHelp/"+ this.module.metadata.id, {}, function(moduleHelp){
		_this.topContainer = $("<div/>", {
			class: 'col-xs-12 top-container'
		})
		_this.panelBody.append(_this.topContainer)
		if(msg){
			_this.topContainer.append($("<span/>", {
				class: 'flash-message',
				html: msg
			}))
		}

		_this.helpTextTable = $("<table class='table help-text-table'></table>")
		if(moduleHelp.helpText){
			// helpText.append($("<thead><tr><th>Help Text</th></tr></thead>"))
			_this.helpTextTable.append($("<tr><td><div class='help-text'></div></td></tr>"))
			_this.helpTextTable.find(".help-text").html(moduleHelp.helpText)
		} else {
			_this.helpTextTable.append($("<thead><tr><th><div class='no-help-text'>No Help Text</div></th></tr></thead>"))
		}
		_this.panelBody.append(_this.helpTextTable)

		_this.inputs = $("<table class='table input-table'></table>")
		if(moduleHelp.inputNames && moduleHelp.inputNames.length){
			_this.inputs.append($("<thead><tr><th>Inputs</th><th>Name</th><th>Description</th></tr></thead>"))
			var tbody = $("<tbody></tbody>")
			$.each(moduleHelp.inputNames, function(i, inputName){
				_this.inputs.append($("<tr><td></td><td class='name'>"+inputName+"</td><td class='value input-description'><span>"+moduleHelp.inputs[inputName]+"</span></td></tr>"))
			})
		} else {
			_this.inputs.append($("<thead><tr><th>No Input Helps</th></tr></thead>"))
		}
		_this.panelBody.append(_this.inputs)

		_this.outputs = $("<table class='table output-table'></table>")
		if(moduleHelp.outputNames && moduleHelp.outputNames.length){
			_this.outputs.append($("<thead><tr><th>Outputs</th><th>Name</th><th>Description</th></tr></thead>"))
			$.each(moduleHelp.outputNames, function(i, outputName){
				_this.outputs.append($("<tr><td></td><td class='name'>"+outputName+"</td><td class='value output-description'><span>"+moduleHelp.outputs[outputName]+"</span></td></tr>"))
			})
		} else {
			_this.outputs.append($("<thead><tr><th>No Output Helps</th></tr></thead>"))
		}
		_this.panelBody.append(_this.outputs)

		_this.params = $("<table class='table param-table'></table>")
		if(moduleHelp.paramNames && moduleHelp.paramNames.length){
			_this.params.append($("<thead><tr><th>Parameters</th><th>Name</th><th>Description</th></tr></thead>"))
			var tbody = $("<tbody></tbody>")
			$.each(moduleHelp.paramNames, function(i, paramName){
				_this.params.append($("<tr><td></td><td class='name'>"+paramName+"</td><td class='value param-description'><span>"+moduleHelp.params[paramName]+"</span></td></tr>"))
			})
		} else {
			_this.params.append($("<thead><tr><th>No Parameter Helps</th></tr></thead>"))
		}
		_this.panelBody.append(_this.params)

		$.getJSON(_this.url +"/canEdit/"+ _this.module.metadata.id, {}, function(canEdit){
			if(canEdit.success){
				_this.editBtn = $("<button/>", {
					class: "btn btn-default btn-sm edit-btn",
					text: "Edit help"
				})
				_this.editBtn.click(function(){
					$(this).hide()
					_this.saveBtn.show()
					_this.edit()
				})
				_this.panelBody.find(".top-container").append(_this.editBtn)
				_this.saveBtn = $("<button/>", {
					class: "btn btn-primary btn-sm save-btn",
					text: "Save edits"
				})
				_this.saveBtn.click(function(){
					$(this).hide()
					_this.editBtn.show()
					_this.save()
				})
				_this.panelBody.find(".top-container").append(_this.saveBtn)
				_this.saveBtn.hide()
			}
		})
	})
	
}
Module.prototype.edit = function() {
	$.each(this.panelBody.find(".value"), function(i, el){
		$(el).append($("<input/>", {
			type: "text",
			value: $(el).html(),
			width: "100%"
		}))
		$(el).find("span").hide()
	})
	if(this.helpTextTable.find(".no-help-text").length){
		this.helpTextTable.find(".no-help-text").remove()
		this.helpTextTable.append($("<tbody><tr><td><div class='help-text'>No help text</div></td></tr></tbody>"))
	}
	this.helpTextTable.find(".help-text").parent().append($("<textarea class='module-help' style='width:100%; height:300px; resize:vertical;'>"+this.helpTextTable.find(".help-text").html()+"</textarea>"))
	this.helpTextTable.find(".help-text").hide()
}
Module.prototype.save = function(){
	var _this = this
	var moduleHelp = this.makeHelp()

	$.ajax({
	    type: 'POST',
	    url: _this.url +"/jsonSetModuleHelp/",
	    dataType: 'json',
	    success: function(data) {
	    	_this.panelBody.empty()
	    	var msg = (data.success ? "Module help successfully saved." : "An error has occurred.")
			_this.renderHelp(msg)
	    },
	    error: function(jqXHR, textStatus, errorThrown) {
	    	_this.panelBody.find(".flash-message").append("An error has occurred.")
	    },
	    data: {id:_this.module.metadata.id, jsonHelp:JSON.stringify(moduleHelp)}
	})
}

Module.prototype.makeHelp = function() {
	var paramTable = this.panelBody.find(".param-table"),
		inputTable = this.panelBody.find(".input-table"),
		outputTable = this.panelBody.find(".output-table"),
		result = {params:{}, paramNames:[], inputs:{}, inputNames:[], outputs:{}, outputNames:[]}
	
	result.helpText = this.panelBody.find("textarea.module-help").val()
	paramTable.find("tbody tr").each(function(i,row) {
		var name = $(row).find("td.name").text()
		var value = $(row).find("td.value input").val()
		if (value!=null && value!="") {
			result.paramNames.push(name)
			result.params[name] = value
		}
	})
	inputTable.find("tbody tr").each(function(i,row) {
		var name = $(row).find("td.name").text()
		var value = $(row).find("td.value input").val()
		if (value!=null && value!="") {
			result.inputNames.push(name)
			result.inputs[name] = value
		}
	})
	outputTable.find("tbody tr").each(function(i,row) {
		var name = $(row).find("td.name").text()
		var value = $(row).find("td.value input").val()
		if (value!=null && value!="") {
			result.outputNames.push(name)
			result.outputs[name] = value
		}
	})
	return result;
}

exports.ModuleBrowser = ModuleBrowser

})(typeof(exports) !== 'undefined' ? exports : window)
