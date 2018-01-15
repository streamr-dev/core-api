package com.unifina.domain.dashboard

import com.unifina.domain.security.Permission
import groovy.transform.CompileStatic

class Dashboard {

	SecUser user

	String name

	Date dateCreated
	Date lastUpdated

	SortedSet<DashboardItem> items

	static hasMany = [
		items: DashboardItem,
		permissions: Permission
	]

	static constraints = {
		name(nullable:true)
	}

	static mapping = {
		items cascade: 'all-delete-orphan'
	}

	@CompileStatic
	Map toSummaryMap() {
		[
			id: id,
			name: name,
			numOfItems: items == null ? 0 : items.size(),
		]
	}

	@CompileStatic
	Map toMap() {
		[
			id: id,
			name: name,
			items: items == null ? [] : items.collect { DashboardItem it -> it.toMap() },
		]
	}

}
