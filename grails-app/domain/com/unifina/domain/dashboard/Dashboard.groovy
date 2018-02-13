package com.unifina.domain.dashboard

import com.unifina.utils.IdGenerator
import grails.converters.JSON
import com.unifina.domain.security.Permission
import groovy.transform.CompileStatic

class Dashboard {

	String id
	String name

	Date dateCreated
	Date lastUpdated

	String layout = "{}" // JSON

	static hasMany = [
		items: DashboardItem,
		permissions: Permission
	]

	static constraints = {
		name nullable: true, blank: false
		layout nullable: true
	}

	static mapping = {
		items cascade: "all-delete-orphan"
		id generator: IdGenerator.name
		layout type: "text"
	}

	@CompileStatic
	Map toSummaryMap() {
		[
				id        : id,
				name      : name,
				numOfItems: items == null ? 0 : items.size(),
		]
	}

	@CompileStatic
	Map toMap() {
		[
				id    : id,
				name  : name,
				items : items == null ? [] : items.collect { DashboardItem it -> it.toMap() },
				layout: layout ? JSON.parse(layout) : [:]
		]
	}
}
