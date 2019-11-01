package com.unifina.api

import com.unifina.domain.security.SecUser
import grails.validation.Validateable

@Validateable
class StartCanvasAsAdminParams {
	SecUser startedBy

	static constraints = {
		startedBy(nullable: false)
	}
}
