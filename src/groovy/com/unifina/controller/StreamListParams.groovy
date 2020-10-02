package com.unifina.controller

import com.unifina.domain.Permission
import com.unifina.service.ListParams
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class StreamListParams extends ListParams {
	String name
	Boolean uiChannel

	StreamListParams() {
		super()
		operation = Permission.Operation.STREAM_GET
	}

	static constraints = {
		name(nullable: true, blank: false)
		uiChannel(nullable: true)
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
			// Filter by UI channel
			if (uiChannel != null) {
				eq("uiChannel", uiChannel)
			}
		}
	}

	@CompileStatic
	Map toMap() {
		return super.toMap() + ([
			name     : name,
			uiChannel: uiChannel,
		] as Map<String, Object>)
	}
}
