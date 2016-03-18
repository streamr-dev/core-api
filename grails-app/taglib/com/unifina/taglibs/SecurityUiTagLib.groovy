/* Copyright 2009-2013 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.unifina.taglibs

import grails.util.Holders

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class SecurityUiTagLib {

	static namespace = 's2ui'

	// TODO check required attrs

	def checkboxRow = { attrs ->

		String labelCodeDefault = attrs.remove('labelCodeDefault')
		String name = getRequiredAttribute(attrs, 'name', 'checkboxRow')
		String labelCode = getRequiredAttribute(attrs, 'labelCode', 'checkboxRow')
		def value = getRequiredAttribute(attrs, 'value', 'checkboxRow')
		def bean = getRequiredAttribute(attrs, 'bean', 'checkboxRow')

		def fieldAttributes = [name: name, value: value] + attrs
		out << """
		<tr class="prop">
			<td valign="top" class="name">
			<label for="${name}">${message(code: labelCode, default: labelCodeDefault)}</label>
			</td>
			<td valign="top" class="value ${hasErrors(bean: bean, field: name, 'errors')}">
			${checkBox(fieldAttributes)}
			${fieldErrors(bean: bean, field: name)}
			</td>
		</tr>
		"""
	}

	def textFieldRow = { attrs ->
		String labelCodeDefault = attrs.remove('labelCodeDefault')
		String name = getRequiredAttribute(attrs, 'name', 'textFieldRow')
		String labelCode = getRequiredAttribute(attrs, 'labelCode', 'textFieldRow')
		def value = getRequiredAttribute(attrs, 'value', 'textFieldRow')
		def bean = getRequiredAttribute(attrs, 'bean', 'textFieldRow')

		def fieldAttributes = [name: name, value: value] + attrs
		out << """
		<tr class="prop">
			<td valign="top" class="name">
			<label for="${name}">${message(code: labelCode, default: labelCodeDefault)}</label>
			</td>
			<td valign="top" class="value ${hasErrors(bean: bean, field: name, 'errors')}">
			${textField(fieldAttributes)}
			${fieldErrors(bean: bean, field: name)}
			</td>
		</tr>
		"""
	}

	def passwordFieldRow = { attrs ->
		String labelCodeDefault = attrs.remove('labelCodeDefault')
		String name = getRequiredAttribute(attrs, 'name', 'passwordFieldRow')
		String labelCode = getRequiredAttribute(attrs, 'labelCode', 'passwordFieldRow')
		def value = getRequiredAttribute(attrs, 'value', 'passwordFieldRow')
		def bean = getRequiredAttribute(attrs, 'bean', 'passwordFieldRow')

		def fieldAttributes = [name: name, value: value] + attrs
		out << """
		<tr class="prop">
			<td valign="top" class="name">
			<label for="${name}">${message(code: labelCode, default: labelCodeDefault)}</label>
			</td>
			<td valign="top" class="value ${hasErrors(bean: bean, field: name, 'errors')}">
			${passwordField(fieldAttributes)}
			${fieldErrors(bean: bean, field: name)}
			</td>
		</tr>
		"""
	}

	def dateFieldRow = { attrs ->
		String labelCodeDefault = attrs.remove('labelCodeDefault')
		String name = getRequiredAttribute(attrs, 'name', 'dateFieldRow')
		String labelCode = getRequiredAttribute(attrs, 'labelCode', 'dateFieldRow')
		def value = getRequiredAttribute(attrs, 'value', 'dateFieldRow')
		def bean = getRequiredAttribute(attrs, 'bean', 'dateFieldRow')

		value = formatDate(date: value)

		def fieldAttributes = [name: name, value: value, maxlength: '20', 'class': 'date_input'] + attrs
		out << """
		<tr class="prop">
			<td valign="top" class="name">
			<label for="${name}">${message(code: labelCode, default: labelCodeDefault)}</label>
			</td>
			<td valign="top" class="value ${hasErrors(bean: bean, field: name, 'errors')}">
			${textField(fieldAttributes)}
			${fieldErrors(bean: bean, field: name)}
			</td>
		</tr>
		"""

		writeDocumentReady out, "\$('#${name}').date_input();"
	}

	def submitButton = { attrs ->
		String form = getRequiredAttribute(attrs, 'form', 'submitButton')
		String elementId = getRequiredAttribute(attrs, 'elementId', 'submitButton')
		String text = resolveText(attrs)

		def writer = getOut()
		writeRemainingAttributes writer, attrs
		writer << "<button type='submit' id='${elementId}_submit'>${text}</button>\n"

		String javascript = """
			\$('#${elementId}').bind('click', function() {
			   document.forms.${form}.submit();
			});
		"""
		writeDocumentReady writer, javascript
	}

	// TODO remove
	def linkButton = { attrs ->
		String text = resolveText(attrs)
		String elementId = getRequiredAttribute(attrs, 'elementId', 'linkButton')

		def writer = getOut()
		writer <<  """<a href="${createLink(attrs).encodeAsHTML()}" id="${elementId}" """

		writeRemainingAttributes writer, attrs
		writer << ">${text}</a>"

		writeDocumentReady writer, javascript
	}

	def tabs = { attrs, body ->

		String id = getRequiredAttribute(attrs, 'elementId', 'tabs')
		def data = getRequiredAttribute(attrs, 'data', 'tabs')
		def height = getRequiredAttribute(attrs, 'height', 'tabs')

		def writer = getOut()
		writer << """<div id="${id}">\n"""
		writer << "<ul>\n"
		for (element in data) {
			writer << """<li><a href="#tab-${element.name}" class="icon ${element.icon}">\n"""
			writer << message(code: element.messageCode)
			writer << '</a></li>\n'
		}
		writer << "</ul>\n"

		writer << body()
		writer << "</div>\n"

		String javascript = """
			\$("#${id}").tabs().show().resizable({minHeight: ${height}, minWidth: 100});
		"""
		writeDocumentReady writer, javascript
	}

	def tab = { attrs, body ->

		String name = getRequiredAttribute(attrs, 'name', 'tab')
		def height = getRequiredAttribute(attrs, 'height', 'tab')

		out << """
		<div id="tab-${name}">
			<div class="s2ui_section" style="height: ${height}px; overflow: auto">
			${body()}
			</div>
		</div>
		"""
	}

	def deleteButtonForm = { attrs ->

		def id = getRequiredAttribute(attrs, 'instanceId', 'deleteButton')

		attrs.action = 'delete'

		out << """
			<form action='${createLink(attrs)}' method='POST' name='deleteForm'>
				<input type="hidden" name="id" value="${id}" />
				<button id="deleteButton">${message(code:'default.button.delete.label')}</button>
			</form>

			<script>

			\$(document).ready(function() {
				\$("#deleteButton").bind('click', function(e) {
					e.preventDefault()
					var c = confirm("Are you sure?");
					if (c == true)
						document.forms.deleteForm.submit();
					else
						return false
				});
			});
			</script>
		"""
	}

	def required = { attrs ->
		out << "<span class='s2ui_required'>*&nbsp;</span>"
	}

	def fieldErrors = { attrs ->
		def bean = attrs.remove('bean')
		if (bean) {
			out << eachError(attrs, {
				out << "<span class='s2ui_error'>${message(error:it)}</span>"
			})
		}
	}

	def paginationSummary = { attrs ->
		int total = getRequiredAttribute(attrs, 'total', 'paginationSummary').toInteger()
		if (total == 0) {
			out << "No results"
			return
		}

		int max = params.int('max')
		int offset = params.int('offset')

		int from = offset + 1
		int to = offset + max
		if (to > total) {
			to = total
		}
		out << "Showing $from through $to out of ${total}."
	}

	def showFlash = { attrs ->
		String message = flash.remove('message')
		String error = flash.remove('error')
		if (!message && !error) {
			return
		}

		String type = message ? 'info' : 'error'
		String text = message ?: error
		out << """
		<script>
		\$(document).ready(function() {
			SpringSecurityUI.message('${type}', "${text.encodeAsHTML()}", 3000);
		});
		</script>
		"""
	}

	def form = { attrs, body ->
		boolean center = attrs.remove('center') == 'true'
		boolean resizeable = attrs.remove('resizeable') != 'false'
		def width = getRequiredAttribute(attrs, 'width', 'form')
		if (width.endsWith('%')) {
			center = true
			resizeable = true
		}
		else {
			width = width.toInteger().toString() + 'px'
		}
		int height = getRequiredAttribute(attrs, 'height', 'form').toInteger()
		String titleCode = getRequiredAttribute(attrs, 'titleCode', 'form')
		def titleCodeArgs = attrs.remove('titleCodeArgs')
		String elementId = getRequiredAttribute(attrs, 'elementId', 'form')

		String classes = "ui-widget-content"
		if (center) classes += ' s2ui_center'

		String title = titleCodeArgs ? message(code: titleCode, args: titleCodeArgs) : message(code: titleCode)

		out << """
		<div class="$classes" id="${elementId}"
		     style="width: ${width}; height: ${height}px; padding: 5px; text-align: center; position: relative;">

		<div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix s2ui_center" style='padding: 10px'>
		<span style="-moz-user-select: none;" unselectable="on" class="ui-dialog-title">
		${title}
		</span>
		</div>

		${body()}

		</div>
		"""

		if (resizeable) {
			writeDocumentReady out, "\$('#${elementId}').resizable();"
		}
	}

	protected void writeDocumentReady(writer, String javascript) {
		writer << """
		<script>
		\$(document).ready(function() {
			${javascript}
		});
		</script>
		"""
	}

	protected String resolveText(attrs) {
		String messageCode = attrs.remove('messageCode')
		if (messageCode) {
			return message(code: messageCode)
		}

		return attrs.remove('text')
	}

	protected getRequiredAttribute(attrs, String name, String tagName) {
		if (!attrs.containsKey(name)) {
			throwTagError("Tag [$tagName] is missing required attribute [$name]")
		}
		attrs.remove name
	}

	protected void writeRemainingAttributes(writer, attrs) {
		writer << attrs.collect { k, v -> """ $k="$v" """ }.join('')
	}
}
