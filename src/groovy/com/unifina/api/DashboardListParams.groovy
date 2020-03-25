package com.unifina.api

import com.unifina.domain.security.Permission
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class DashboardListParams extends ListParams {
	String name

	DashboardListParams() {
		super()
		operation = Permission.Operation.DASHBOARD_GET
	}

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
