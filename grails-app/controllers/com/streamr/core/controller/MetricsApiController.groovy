package com.streamr.core.controller


import com.streamr.core.service.MetricsService
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
