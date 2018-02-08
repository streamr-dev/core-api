package com.unifina.api

import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
abstract class ListParams {
	String search
	String sortBy
	String order = "asc"
	Integer max = 100
	Integer offset
	Boolean publicAccess = false

	static constraints = {
		search(nullable: true, blank: false)
		sortBy(nullable: true, blank: false)
		order(inList: ["asc", "desc"], nullable: false)
		max(min: 1, max: 100)
		offset(min: 0, nullable: true)
		publicAccess(nullable: false)
	}

	protected abstract String getSearchFields()
	protected abstract Closure additionalCriteria()

	Closure createListCriteria() {
		return {
			if (search) {
				or {
					searchFields.each {
						like(it, "%${search}%")
					}
				}
			}
			if (sortBy) {
				'order'(sortBy, order)
			}
		} << additionalCriteria()
	}

	@CompileStatic
	Map toMap() {
		[
		    search: search,
			sortBy: sortBy,
			order: order,
			max: max,
			offset: offset,
			publicAccess: publicAccess
		]
	}
}
