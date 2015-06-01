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
						format: 'yyyy-mm-dd',
						autoclose: true
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
	 * Renders a Bootstrap panel and generates an id for it automatically. The id is used with the bootstrap scrollspy.
	 * @attr title REQUIRED Title of the panel
	 * @attr class Classes added to the panel
	 */
	def scrollSpyPanel = {attrs, body->
		out << "<div id='${ attrs.title.replaceAll(' ', '').toLowerCase() }' class='panel ${attrs.class ?: ''}'>"
		out << "<div class='panel-heading'>"
		out << "<span class='panel-title'>${attrs.title}</span>"
		out << "</div>"
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
	def clickableTable = {attrs, body->
		out << "<table class='table-clickable table table-striped table-bordered table-hover table-condensed table-responsive'>"
		out << body()
		out << "</table>"
	}
	
	/**
	 * Renders a clickable table row
	 *
	 * @attr link REQUIRED url that the row links to
	 * @attr id id of the domain object
	 * @attr title the title of the domain object
	 */
	def clickableRow = {attrs, body->
		out << "<tr ${attrs.title ? "title='${attrs.title}'" : ""} data-link='${attrs.link}' ${attrs.id ? "data-id='${attrs.id}'" : ""}>"
		out << body()
		out << "</tr>"
	}
	
	/**
	 * Renders a bootstrap style sidebar which can be used e.g. with scrollspy. Can't be used with sub classes
	 *
	 * @attr title REQUIRED titles to be rendered
	 */
	def sidebarNav = {attrs ->
		out << "<nav class='streamr-sidebar'>"
		out << "<ul class='nav'>"
		attrs.titles.each {title ->
			out << "<li>"
			out << "<a href='#${ title.replaceAll(' ', '').toLowerCase() }'>${ title }</a>"
			out << "</li>"
		}
		out << "</ul>"
		out << "</nav>"
	}

}


