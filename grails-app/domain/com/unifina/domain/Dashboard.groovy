package com.unifina.domain

import com.unifina.utils.IdGenerator
import grails.converters.JSON
import grails.persistence.Entity
import groovy.transform.CompileStatic

@Entity
class Dashboard {
	public final static String DEFAULT_NAME = "Untitled Dashboard"
	String id
	String name = DEFAULT_NAME

	Date dateCreated
	Date lastUpdated

	String layout = "{}" // JSON

	static hasMany = [
		permissions: Permission
	]

	static constraints = {
		name nullable: true, blank: false
		layout nullable: true
	}

	static mapping = {
		id generator: IdGenerator.name
		layout type: "text"
	}

	@CompileStatic
	Map toSummaryMap() {
		[
			id     : id,
			name   : name,
			created: dateCreated,
			updated: lastUpdated
		]
	}

	@CompileStatic
	Map toMap() {
		[
			id     : id,
			name   : name,
			layout : layout ? JSON.parse(layout) : [:],
			created: dateCreated,
			updated: lastUpdated
		]
	}
}
