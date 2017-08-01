package com.unifina.api

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.Webcomponent
import grails.converters.JSON
import grails.validation.Validateable

@Validateable
class SaveDashboardItemCommand {
	String id
	String title
	Dashboard dashboard
	Canvas canvas
	Integer module
	String webcomponent

	static constraints = {
		title(blank: false)
		canvas(blank: false)
		module(nullable: false)
		webcomponent validator: { val ->
			if (Webcomponent.getByName(val) == null) {
				return false
			}
		}
	}

	def getWebcomponent() {
		webcomponent ?: JSON.parse(canvas.json)?.modules?.find { it.hash == module }?.uiChannel?.webcomponent
	}

	Map toMap() {
		return getProperties().subMap(["id", "title", "dashboard", "canvas", "module", "webcomponent"])
	}
}
