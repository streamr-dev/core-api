package com.unifina.api

import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class DashboardListParams extends ListParams {
	String name

	static constraints = {
		name(nullable: true, blank: false)
	}

	@Override
	protected List<String> getSearchFields() {
		return ["name"]
	}

	@Override
	protected Closure additionalCriteria() {
		return {
			// Filter by exact name
			if (name) {
				eq("name", name)
			}
		}
	}

	@CompileStatic
	Map toMap() {
		super.toMap() + ((Map) [name: name])
	}
}
