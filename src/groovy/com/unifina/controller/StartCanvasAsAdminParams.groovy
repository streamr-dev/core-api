package com.unifina.controller

import com.unifina.domain.User
import grails.validation.Validateable

@Validateable
class StartCanvasAsAdminParams {
	User startedBy

	static constraints = {
		startedBy(nullable: false)
	}
}
