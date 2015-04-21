package com.unifina.domain.dashboard

import java.util.Date;

import com.unifina.domain.security.SecUser

class Dashboard {

	SecUser user
	
	String name
	
	Date dateCreated
	Date lastUpdated
	
	SortedSet items
	
	static hasMany = [items: DashboardItem]
	
	static constraints = {
		name(nullable:true)
	}
	
	static mapping = {
		items cascade: 'all-delete-orphan'
	}
	
}
