package com.unifina.service

import com.unifina.data.EventQueueMetrics
import grails.compiler.GrailsCompileStatic

import javax.management.ObjectName
import java.lang.management.ManagementFactory

@GrailsCompileStatic
class MetricsService {
	SignalPathService signalPathService

	int numOfRunningCanvases() {
		signalPathService.runningSignalPaths.size()
	}

	int bytesUsedByRunningCanvases() {
		return 0 // TODO
	}

	EventProcessingMetrics fetchEventProcessingMetrics() {
		List<EventQueueMetrics> metrics = signalPathService.runningSignalPaths
			*.getGlobals()
			*.getDataSource()
			*.retrieveMetricsAndReset()

		return new EventProcessingMetrics(
			EventQueueMetrics.aggregateEventsPerSecond(metrics),
			EventQueueMetrics.aggregateMeanProcessingDelay(metrics))
	}

	Object numOfSessionsTomcat() {
		ObjectName name = new ObjectName("Tomcat:type=Manager,context=/streamr-core,host=localhost")
		(Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(name, "activeSessions")
	}

	static class EventProcessingMetrics {
		final double eventsPerSecond
		final double avgProcessingDelay

		EventProcessingMetrics(double eventsPerSecond, double avgProcessingDelay) {
			this.eventsPerSecond = eventsPerSecond
			this.avgProcessingDelay = avgProcessingDelay
		}
	}
}
