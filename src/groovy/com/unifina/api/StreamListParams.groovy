package com.unifina.api

import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class StreamListParams extends ListParams {
	String name
	Boolean uiChannel
	Boolean inbox = false

	static constraints = {
		name(nullable: true, blank: false)
		uiChannel(nullable: true)
		inbox(nullable: true)
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
			// Filter by inbox stream
			if (inbox != null) {
				eq("inbox", inbox)
			}
		}
	}

	@CompileStatic
	Map toMap() {
		super.toMap() + [
			name: name,
			uiChannel: uiChannel,
			inbox: inbox
		]
	}
}
