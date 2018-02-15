package com.unifina.api

import com.unifina.domain.security.IntegrationKey
import grails.validation.Validateable
import groovy.transform.CompileStatic

@Validateable
class IntegrationKeyListParams extends ListParams {
	IntegrationKey.Service service

	static constraints = {
		service(nullable: true)
	}

	@Override
	protected List<String> getSearchFields() {
		return []
	}

	@Override
	protected Closure additionalCriteria() {
		return {
			// Filter by exact service
			if (service) {
				eq("service", service)
			}
		}
	}

	@CompileStatic
	Map toMap() {
		super.toMap() + ((Map) [service: service?.toString()])
	}
}
