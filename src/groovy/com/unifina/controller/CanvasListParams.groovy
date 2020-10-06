package com.unifina.controller

import com.unifina.domain.Canvas
import com.unifina.domain.Permission
import com.unifina.service.ListParams
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class CanvasListParams extends ListParams {
	String name
	Boolean adhoc
	Canvas.State state

	CanvasListParams() {
		super()
		operation = Permission.Operation.CANVAS_GET
	}

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
			if (adhoc != null) {
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
		return super.toMap() + ([
			name : name,
			adhoc: adhoc,
			state: state?.toString()
		] as Map<String, Object>)
	}
}
