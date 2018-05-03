package com.unifina.api

import com.unifina.domain.security.Permission
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
abstract class ListParams {
	public static final int MAX_LIMIT = 1000

	String search
	String sortBy
	String order = "asc"
	Integer max = MAX_LIMIT
	Integer offset = 0
	Boolean grantedAccess = true
	Boolean publicAccess = false
	Permission.Operation operation = Permission.Operation.READ

	static constraints = {
		search(nullable: true, blank: false)
		sortBy(nullable: true, blank: false)
		order(inList: ["asc", "desc"], nullable: false)
		max(min: 1, max: MAX_LIMIT)
		offset(min: 0, nullable: false)
		grantedAccess(nullable: false)
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
			firstResult(offset)
			maxResults(max)
		} << additionalCriteria()
	}

	@CompileStatic
	Map toMap() {
		[
		    search: search,
			sortBy: sortBy,
			order: sortBy != null ? order : null,
			max: max,
			offset: offset,
			grantedAccess: grantedAccess,
			publicAccess: publicAccess
		]
	}
}
