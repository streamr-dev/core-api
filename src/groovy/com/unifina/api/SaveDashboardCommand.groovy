package com.unifina.api

import com.unifina.domain.dashboard.DashboardItem
import grails.validation.Validateable

@Validateable
class SaveDashboardCommand {
	Long id
	String name
	SortedSet<DashboardItem> items

	static constraints = {
		name(blank: false)
	}

	Map toMap() {
		[name: name, items: items]
	}
}
