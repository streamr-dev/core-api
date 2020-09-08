package com.unifina.service

import com.unifina.domain.Canvas
import com.unifina.utils.Webcomponent
import grails.converters.JSON
import grails.validation.Validateable

@Validateable
class SaveDashboardItemCommand {

	String id
	String title
	Canvas canvas
	Integer module
	String webcomponent // TODO: can be removed?

	static constraints = {
		id nullable: true
		title blank: false
		canvas blank: false
		module nullable: false
	}

	def getProperties() {
		[
				id			: id,
				title       : title,
				canvas      : canvas,
				module      : module,
				webcomponent: Webcomponent.getByName(canvas?.json ? JSON.parse(canvas.json)?.modules?.find { it.hash == module }?.uiChannel?.webcomponent : null)
		]
	}
}
