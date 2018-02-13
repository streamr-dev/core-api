package com.unifina.api

import com.unifina.domain.signalpath.Canvas
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class CanvasListParams extends ListParams {
	String name
	Boolean adhoc
	Canvas.State state

	static constraints = {
		name(nullable: true, blank: false)
		adhoc(nullable: true)
		state(nullable: true)
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
			// Filter by adhoc
			if (adhoc) {
				eq("adhoc", adhoc)
			}
			// Filter by state
			if (state) {
				eq("state", state)
			}
		}
	}

	@CompileStatic
	Map toMap() {
		super.toMap() + [
			name: name,
			adhoc: adhoc,
			state: state?.toString()
		]
	}
}
