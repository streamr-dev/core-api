package com.unifina.api

import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class StreamListParams extends ListParams {
	String name
	Boolean uiChannel

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
			if (uiChannel) {
				eq("uiChannel", "uiChannel")
			}
		}
	}

	@CompileStatic
	Map toMap() {
		super.toMap() + [
			name: name,
			uiChannel: uiChannel
		]
	}
}
