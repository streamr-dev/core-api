package com.unifina.api

import com.unifina.domain.dashboard.DashboardItem
import grails.validation.Validateable

@Validateable
class SaveDashboardCommand {
	String name
	SortedSet<DashboardItem> items

	static constraints = {
		name(blank: false)
	}
}
