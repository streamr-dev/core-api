package com.unifina.api

import com.unifina.domain.User
import grails.validation.Validateable

@Validateable
class StartCanvasAsAdminParams {
	User startedBy

	static constraints = {
		startedBy(nullable: false)
	}
}
