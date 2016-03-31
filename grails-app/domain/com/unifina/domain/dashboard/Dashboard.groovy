package com.unifina.domain.dashboard

import com.unifina.domain.security.SecUser

class Dashboard {

	SecUser user
	
	String name
	
	Date dateCreated
	Date lastUpdated
	
	SortedSet<DashboardItem> items
	
	static hasMany = [items: DashboardItem]
	
	static constraints = {
		name(nullable:true)
	}
	
	static mapping = {
		items cascade: 'all-delete-orphan'
	}

	def toSummaryMap() {
		[
			id: id,
		    name: name,
			numOfItems: items == null ? 0 : items.size(),
		]
	}

	def toMap() {
		[
			id: id,
			name: name,
			items: items == null ? [] : items*.toMap(),
		]
	}
	
}
