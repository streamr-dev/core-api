package com.unifina.api

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.signalpath.Canvas
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class SaveDashboardItemCommand {
	String title
	Canvas canvas
	Integer module
	String webcomponent
	int ord
	String size

	static constraints = {
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

	@CompileStatic
	void copyValuesTo(DashboardItem dashboardItem) {
		dashboardItem.title = title
		dashboardItem.canvas = canvas
		dashboardItem.module = module
		dashboardItem.updateWebcomponent()
	}
}
