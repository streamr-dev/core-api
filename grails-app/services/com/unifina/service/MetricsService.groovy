package com.unifina.service

import com.unifina.data.EventQueueMetrics
import grails.compiler.GrailsCompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.management.ObjectName
import java.lang.management.ManagementFactory

class MetricsService {
	static transactional = false

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
		List<List<EventQueueMetrics>> metrics = [true, false].collect { realtime ->
			signalPathService.runningSignalPaths
				.findAll { it.globals.isRealtime() == realtime }
				.collect { it.globals.dataSource.retrieveMetricsAndReset() }
		}

		return new EventProcessingMetrics(
			EventQueueMetrics.aggregateEventsPerSecond(metrics[0]),
			EventQueueMetrics.aggregateMeanProcessingDelay(metrics[0]),
			EventQueueMetrics.aggregateEventsPerSecond(metrics[1]) // for historical only events per sec
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
