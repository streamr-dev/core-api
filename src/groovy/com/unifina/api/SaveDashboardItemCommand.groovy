package com.unifina.api

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.signalpath.Canvas
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class SaveDashboardItemCommand {
	String title
	String canvas
	Integer module
	//String webcomponent TODO: inferred
	int ord
	String size

	static constraints = {
		title(blank: false)
		canvas(blank: false)
		module(nullable: false)
		ord(min: 0)
		size(inList: ["small", "medium", "large"])
	}

	@CompileStatic
	DashboardItem toDashboardItem() {
		def item = new DashboardItem(
			title: title,
			canvas: Canvas.get(canvas),
			module: module,
			ord: ord,
			size: size
		)
		item.updateWebcomponent()
		return item
	}

	@CompileStatic
	void copyValuesTo(DashboardItem dashboardItem) {
		dashboardItem.title = title
		dashboardItem.canvas = Canvas.get(canvas)
		dashboardItem.module = module
		dashboardItem.ord = ord
		dashboardItem.size = size
		dashboardItem.updateWebcomponent()
	}
}
