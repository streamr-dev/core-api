package com.unifina.api

import com.unifina.domain.dashboard.DashboardItem
import grails.validation.Validateable

@Validateable
class SaveDashboardCommand {
	Long id
	String name
	SortedSet<DashboardItem> items

	static constraints = {
		id(nullable: true, blank: false)
		name(blank: false)
		items(nullable: true)
	}

	Map toMap() {
		[name: name, items: items]
	}
}
