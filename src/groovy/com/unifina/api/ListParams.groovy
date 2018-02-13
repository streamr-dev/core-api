package com.unifina.api

import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
abstract class ListParams {
	public static final int MAX_LIMIT = 100

	String search
	String sortBy
	String order = "asc"
	Integer max = MAX_LIMIT
	Integer offset
	Boolean publicAccess = false

	static constraints = {
		search(nullable: true, blank: false)
		sortBy(nullable: true, blank: false)
		order(inList: ["asc", "desc"], nullable: false)
		max(min: 1, max: MAX_LIMIT)
		offset(min: 0, nullable: true)
		publicAccess(nullable: false)
	}

	protected abstract List<String> getSearchFields()
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
			if (offset) {
				firstResult(offset)
			}
			maxResults(max)
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
