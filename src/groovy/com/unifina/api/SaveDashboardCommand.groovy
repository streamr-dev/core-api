package com.unifina.api

import com.unifina.domain.dashboard.DashboardItem
import grails.converters.JSON
import grails.validation.Validateable
import org.json.JSONObject

@Validateable
class SaveDashboardCommand {
	String id

	String name
	SortedSet<DashboardItem> items
	String layout

	static constraints = {
		name(blank: false)
		layout(blank:false)
	}

	Map toMap() {
		[id: id, name: name, items: items, layout: layout]
	}
}
