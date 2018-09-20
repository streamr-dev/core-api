package com.unifina.service

import com.unifina.data.EventQueueMetrics
import com.unifina.data.HistoricalEventQueueMetrics
import com.unifina.data.RealTimeEventQueueMetrics
import grails.compiler.GrailsCompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.management.ObjectName
import java.lang.management.ManagementFactory

class MetricsService {
	GrailsApplication grailsApplication
	SignalPathService signalPathService

	@GrailsCompileStatic
	int numOfRunningCanvases() {
		signalPathService.runningSignalPaths.size()
	}

	@GrailsCompileStatic
	int bytesUsedByRunningCanvases() {
		return 0 // TODO
	}

	@GrailsCompileStatic
	EventProcessingMetrics fetchEventProcessingMetrics() {
		List<EventQueueMetrics> metrics = signalPathService.runningSignalPaths
			*.getGlobals()
			*.getDataSource()
			*.retrieveMetricsAndReset()

		return new EventProcessingMetrics(
			RealTimeEventQueueMetrics.aggregateEventsPerSecond(metrics),
			RealTimeEventQueueMetrics.aggregateMeanProcessingDelay(metrics),
			HistoricalEventQueueMetrics.aggregateEventsPerSecond(metrics)
		)
	}

	Object numOfSessionsTomcat() {
		ObjectName name = new ObjectName(grailsApplication.config.streamr.metrics.numberOfSessions)
		(Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(name, "activeSessions")
	}

	@GrailsCompileStatic
	static class EventProcessingMetrics {
		final double realTimeEventsPerSecond
		final double realTimeAvgProcessingDelay
		final double historicalEventsPerSecond

		EventProcessingMetrics(double realTimeEventsPerSecond,
							   double realTimeAvgProcessingDelay,
							   double historicalEventsPerSecond) {
			this.realTimeEventsPerSecond = realTimeEventsPerSecond
			this.realTimeAvgProcessingDelay = realTimeAvgProcessingDelay
			this.historicalEventsPerSecond = historicalEventsPerSecond
		}
	}
}
