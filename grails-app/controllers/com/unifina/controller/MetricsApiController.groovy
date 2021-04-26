package com.unifina.controller

import com.unifina.service.MetricsService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON

@GrailsCompileStatic
class MetricsApiController {

	MetricsService metricsService

	@StreamrApi(allowRoles = AllowRole.DEVOPS)
	def index() {
		render([
			numOfTomcatSessions: metricsService.numOfSessionsTomcat()
		] as JSON)
	}
}
