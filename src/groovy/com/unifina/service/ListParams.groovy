package com.unifina.service

import com.unifina.controller.StreamListParams
import com.unifina.domain.Permission
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
	String operation

	Permission.Operation operationToEnum() {
		if (operation != null) {
			operation = operation.toUpperCase()
		}
		if (Permission.Operation.validateOperation(operation)) {
			return Permission.Operation.valueOf(operation)
		}
		return null
	}

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
				// For stream table we have full text index for better performance
				if (this instanceof StreamListParams) {
					sqlRestriction("match(name, description) against (?)", [search])
				} else {
					or {
						searchFields.each {
							like(it, "%${search}%")
						}
					}
				}
			}
			if (sortBy) {
				'order'(sortBy, order)
				if (!"id".equals(sortBy)) {
					'order'("id", "asc")
				}
			}
			firstResult(offset)
			maxResults(max)
		} << additionalCriteria()
	}

	@CompileStatic
	Map<String, Object> toMap() {
		return [
			search       : search,
			sortBy       : sortBy,
			order        : sortBy != null ? order : null,
			max          : max,
			offset       : offset,
			grantedAccess: grantedAccess,
			publicAccess : publicAccess
		] as Map<String, Object>
	}
}
