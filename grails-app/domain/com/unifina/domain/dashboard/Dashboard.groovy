package com.unifina.domain.dashboard

import java.util.Date;

import com.unifina.domain.security.SecUser

class Dashboard {

	SecUser user
	
	String name
	
	Date dateCreated
	Date lastUpdated
	
	static hasMany = [items: DashboardItem]
	
	static mapping = {
		items cascade: 'all-delete-orphan'
	}
	
}
