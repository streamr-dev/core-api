package com.unifina.controller.api


import com.unifina.security.StreamrApi
import com.unifina.service.MetricsService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@GrailsCompileStatic
@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class MetricsApiController {

	MetricsService metricsService

	@StreamrApi
	def index() {
		MetricsService.EventProcessingMetrics eventProcessingMetrics = metricsService.fetchEventProcessingMetrics()
		render([
			canvases: [
				running: metricsService.numOfRunningCanvases(),
				totalSerializedBytes: metricsService.bytesUsedByRunningCanvases(),
			],
			eventProcessing: [
				eventsPerSecond: eventProcessingMetrics.eventsPerSecond,
				avgProcessingDelay: eventProcessingMetrics.avgProcessingDelay,
			],
			numOfTomcatSessions: metricsService.numOfSessionsTomcat()
		] as JSON)
	}
}
