package com.unifina.api

import com.unifina.domain.security.SecUser
import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class StartCanvasAsAdminParams {
	SecUser startedBy

	static constraints = {
		startedBy(nullable: false)
	}
}
