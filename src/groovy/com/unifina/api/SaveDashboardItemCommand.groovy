package com.unifina.api

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.Webcomponent
import grails.converters.JSON
import grails.validation.Validateable

@Validateable
class SaveDashboardItemCommand {
	String id
	String title
	Canvas canvas
	Integer module
	String webcomponent

	static constraints = {
		title blank: false
		canvas blank: false
		module nullable: false
	}

	def getProperties() {
		[
				id          : id,
				title       : title,
				canvas      : canvas,
				module      : module,
				webcomponent: Webcomponent.getByName(canvas?.json ? JSON.parse(canvas.json)?.modules?.find { it.hash == module }?.uiChannel?.webcomponent : null)
		]
	}
}
