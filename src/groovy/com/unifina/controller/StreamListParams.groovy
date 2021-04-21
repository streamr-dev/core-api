package com.unifina.controller

import com.unifina.domain.Permission
import com.unifina.service.ListParams
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class StreamListParams extends ListParams {
	String name

	StreamListParams() {
		super()
		operation = Permission.Operation.STREAM_GET.toString()
	}

	static constraints = {
		name(nullable: true, blank: false)
	}

	@Override
	protected List<String> getSearchFields() {
		return ["name", "description"]
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
		return super.toMap() + ([
			name: name,
		] as Map<String, Object>)
	}
}
