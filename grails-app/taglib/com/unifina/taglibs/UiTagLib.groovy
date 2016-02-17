package com.unifina.taglibs

import groovy.transform.CompileStatic

import java.text.SimpleDateFormat

import org.codehaus.groovy.runtime.InvokerHelper


public class UiTagLib {
	
	static namespace = "ui"
	
	/**
	* Renders a jQuery ui datepicker.
	*
	* @attr name REQUIRED the field name/id
	* @attr value a Date object that represents the initial value
	*/
	def datePicker = {attrs, body->
		def name = attrs.name
		def id = attrs.id ?: name
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
		def date = df.format(attrs.value ?: new Date())
		
		// If you want to disable weekends, add this to the datepicker options:
		// beforeShowDay: \$.datepicker.noWeekends
		
		def str = ""
		
		str += """
			<script type="text/javascript">
				\$(document).ready(function() {
					\$("#${id}").datepicker({
						weekStart: 1,
						format: '${message(code:"default.datePicker.format")}',
						autoclose: true,
						startDate: ${attrs.startDate ? 'new Date('+attrs.startDate.getTime()+')' : 'undefined'},
						endDate: ${attrs.endDate ? 'new Date('+attrs.endDate.getTime()+')' : 'undefined'},
						todayBtn: ${attrs.todayBtn && attrs.todayBtn == 'true' ? 'true' : 'false'}
					});
					\$("#${id}").on('change', function() {
						\$(this).datepicker('update')
					});
				});
			</script>
		"""
		
		str += "<input type='text' name='$name' id='$id' value='$date' class='"+attrs.class+"'/>"
		out << str
	}
	
	def flashMessage = {attrs, body->
		if (flash.message) {
			out << "<div class='alert alert-info'>"
			out << "<i class='fa fa-exclamation-circle'></i>"
			out << flash.message
			out << "</div>"
		}
		if (flash.error) {
			out << "<div class='alert alert-danger'>"
			out << "<i class='fa fa-exclamation-triangle'></i>"
			out << flash.error
			out << "</div>"
		}
	}
	
	def pageHeader = {attrs, body->
		out << "<div class='page-header'>"
		out << "<h1>${body()}</h1>"
		out << "</div>"
	}

	/**
	 * Renders a breadcrumb. Body of the tag should be <li> list items.
	 * @attr class Classes added to the breadcrumb
	 */
	def breadcrumb = {attrs, body->
		out << "<div class='breadcrumb breadcrumb-page ${attrs.class ?: ""}'>"
		out << body()
		out << "</div>"
	}
	
	/**
	 * Renders a Bootstrap panel.
	 * @attr title Title of the panel
	 * @attr class Classes added to the panel
	 */
	def panel = {attrs, body->
		out << "<div class='panel ${attrs.class ?: ''}'>"
		if (attrs.title) {
			out << "<div class='panel-heading'>"
			out << "<span class='panel-title'>${attrs.title}</span>"
			out << "</div>"
		}
		out << "<div class='panel-body'>"
		out << body()
		out << "</div>" // end panel body
		out << "</div>" // end panel
	}
	
	/**
	 * Renders a labeled element.
	 * @attr label REQUIRED The label
	 * @attr for In forms, the name of the form input that this label is for
	 * @attr class Classes to be added to the form-group element
	 */
	def labeled = {attrs, body->
		out << """
			<div class="form-group ${attrs.class ?: ''}">
				<label ${attrs.for ? 'for="name"' : ''} class="control-label">
					${attrs.label}
				</label>
			    <div>
			    	${body()}
			    </div>
			</div>
		"""
	}
	
	/**
	 * Renders a radio button
	 *
	 * @attr name REQUIRED the field name/id
	 * @attr value REQUIRED Value of the selection
	 * @attr id DOM id
	 * @attr checked Whether the radio button is selected
	 * @attr class Classes to be added
	 * @attr inline Is this an inline radio button?
	 */
	def radio = {attrs, body->
		out << """
			<div class="${attrs.inline ? 'radio-inline' : 'radio'}">
				<label>
					<input type="radio" id="${attrs.id}" name="${attrs.name}" value="${attrs.value}" ${attrs.checked ? "checked" : ""} class="${attrs.class}">
					<span class="lbl">${body()}</span>
				</label>
			</div>
		"""
	}
	
	/**
	 * Renders a checkbox button
	 *
	 * @attr name REQUIRED the field name/id
	 * @attr value REQUIRED Value of the selection
	 * @attr id DOM id
	 * @attr checked Whether the radio button is selected
	 * @attr class Classes to be added
	 * @attr inline Is this an inline checkbox?
	 */
	def checkbox = {attrs, body->
		out << """
			<div class="${attrs.inline ? 'checkbox-inline' : 'checkbox'}">
				<label>
					<input type="checkbox" id="${attrs.id}" name="${attrs.name}" value="${attrs.value}" ${attrs.checked ? "checked" : ""} class="${attrs.class}">
					<span class="lbl">${body()}</span>
				</label>
			</div>
		"""
	}
	
	def paginate = {attrs, body->
		def offset = params.offset ? params.offset.toInteger() : 0
		def max = attrs.max.toInteger()
		def total = attrs.total.toInteger()
		def page = offset / max + 1
		def pages = total / max

		out << "<ul class='pagination'>"

		if (page > 1)
			out << "<li>"
		else
			out << "<li class='disabled'>"

		out << "<a href='#'>&laquo;</a></li>"

		for (i in 1..pages+1) {
			def ioff = (i - 1) * max
			def ilink = createLink(controller: controllerName, action: actionName, params: [
				offset: ioff,
				max: max
			])

			if (ioff == offset)
				out << "<li class='active'>"
			else
				out << "<li>"

			out << "<a href='${ilink}'>$i</a></li>"
		}

		if (page <= pages)
			out << "<li>"
		else
			out << "<li class='disabled'>"

		out << "<a href='#'>&raquo;</a></li>"

		out << "</ul>"
	}
	
	/**
	 * Renders a table with clickable rows
	 */
	def table = {attrs, body->
		if (!attrs.containsKey('class'))
			attrs.put('class', 'clickable-table table table-striped table-hover table-condensed table-bordered')
		else attrs.put('class', attrs.get('class') + " clickable-table")
			
		out << "<div "
		outputAttributes(attrs, out)
		out << ">"
		out << body()
		out << "</div>"
	}

	def thead = {attrs, body->
		if (!attrs.containsKey('class'))
			attrs.put('class', 'thead')
		else attrs.put('class', attrs.get('class') + " thead")
			
		out << "<div "
		outputAttributes(attrs, out)
		out << ">"
		out << body()
		out << "</div>"
	}
	
	def th = {attrs, body->
		if (!attrs.containsKey('class'))
			attrs.put('class', 'th')
		else attrs.put('class', attrs.get('class') + " th")
			
		out << "<span "
		outputAttributes(attrs, out)
		out << ">"
		out << body()
		out << "</span>"
	}
	
	def tbody = {attrs, body->
		if (!attrs.containsKey('class'))
			attrs.put('class', 'tbody')
		else attrs.put('class', attrs.get('class') + " tbody")
			
		out << "<div "
		outputAttributes(attrs, out)
		out << ">"
		out << body()
		out << "</div>"
	}
	
	def td = {attrs, body->
		if (!attrs.containsKey('class'))
			attrs.put('class', 'td')
		else attrs.put('class', attrs.get('class') + " td")
			
		out << "<span "
		outputAttributes(attrs, out)
		out << ">"
		out << body()
		out << "</span>"
	}
		
		
	/**
	 * Renders a table row that can act as a link.
	 *
	 * @attr link Url that the row links to. If none is given, the row is not rendered as a link.
	 */
	def tr = {attrs, body->
		if (!attrs.containsKey('class'))
			attrs.put('class', 'tr')
		else attrs.put('class', attrs.get('class') + " tr")
	
		def link = attrs.remove('link')
		if (link) {
			out << "<a href='${link}' "
			outputAttributes(attrs, out)
			out << ">"
			out << body()
			out << "</a>"
		}
		else {
			out << "<div "
			outputAttributes(attrs, out)
			out << ">"
			out << body()
			out << "</div>"
		}
	}

	/**
	 * Renders a button that opens a sharePopup (sharing-dialog.js)
	 * Remember to add <r:require module="sharing-dialog"/> to <HEAD>!
	 * @body is button label, just like HTML buttons
	 * @attr url to the resource to be shared; read from data-url HTML attribute if url and getUrl omitted
	 * @attr getUrl javascript command that returns the url to the resource
	 * @attr name of the resource shown in the sharePopup dialog, generated if omitted
	 */
	def shareButton = {attrs, body->
		def extraClass = attrs.remove("class") ?: ""
		def extraOnClick = attrs.remove("onclick") ?: ""
		def name = attrs.remove("name") ?: ""	// generated in sharePopup if omitted
		def url = attrs.remove("url")
		def urlGetter = attrs.remove("getUrl")
		def resourceUrl = url ? '"'+url+'"' : (urlGetter ?: '$(this).data("url")')

		out << "<button class='btn share-button $extraClass' "
		out << "onclick='$extraOnClick;sharePopup($resourceUrl, \"$name\")' "
		outputAttributes(attrs, out)
		out << "><span class='superscript'>+</span><span class='fa fa-user'></span>" << body() << "</button>"

		// http://stackoverflow.com/questions/33461034/call-grails-2-rrequire-module-from-a-taglib
		// should be safe, ResourceTagLib.declareModuleRequiredByPage won't add it second time
		out << r.require([modules: "sharing-dialog"])
	}

	/**
	 * Dump out attributes in HTML compliant fashion.
	 */
	@CompileStatic
	void outputAttributes(Map attrs, Writer writer) {
		attrs.remove('tagName') // Just in case one is left
		attrs.each { k, v ->
			if (v != null) {
				writer << "$k=\"${InvokerHelper.invokeMethod(v.toString(), "encodeAsHTML", null)}\" "
			}
		}
	}
	
}


