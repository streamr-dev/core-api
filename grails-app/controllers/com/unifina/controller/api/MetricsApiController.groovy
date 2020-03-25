package com.unifina.controller.api

import com.unifina.security.AllowRole
import com.unifina.security.StreamrApi
import com.unifina.service.MetricsService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON

@GrailsCompileStatic
class MetricsApiController {

	MetricsService metricsService

	@StreamrApi(allowRoles = AllowRole.DEVOPS)
	def index() {
		MetricsService.EventProcessingMetrics eventProcessingMetrics = metricsService.fetchEventProcessingMetrics()
		render([
			canvases: [
				running: metricsService.numOfRunningCanvases(),
				totalSerializedBytes: metricsService.bytesUsedByRunningCanvases(),
			],
			realTime: [
				eventsPerSecond: eventProcessingMetrics.realTimeEventsPerSecond,
				avgProcessingDelay: eventProcessingMetrics.realTimeAvgProcessingDelay,
			],
			historical: [
			  eventsPerSecond: eventProcessingMetrics.historicalEventsPerSecond
			],
			numOfTomcatSessions: metricsService.numOfSessionsTomcat()
		] as JSON)
	}
}
