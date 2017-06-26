package com.unifina.api

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.signalpath.Canvas
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class SaveDashboardItemCommand {
	String id
	String title
	Canvas canvas
	Integer module
	String webcomponent

	static constraints = {
		id(nullable: false)
		title(blank: false)
		canvas(blank: false)
		module(nullable: false)
	}

	@CompileStatic
	DashboardItem toDashboardItem() {
		def item = new DashboardItem(
			title: title,
			canvas: canvas,
			module: module
		)
		item.updateWebcomponent()
		return item
	}
}
