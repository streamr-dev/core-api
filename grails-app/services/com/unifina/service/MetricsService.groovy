package com.unifina.service

import com.unifina.data.EventQueueMetrics
import com.unifina.data.HistoricalEventQueueMetrics
import com.unifina.data.RealTimeEventQueueMetrics
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
			RealTimeEventQueueMetrics.aggregateEventsPerSecond(metrics),
			RealTimeEventQueueMetrics.aggregateMeanProcessingDelay(metrics),
			HistoricalEventQueueMetrics.aggregateEventsPerSecond(metrics)
		)
	}

	Object numOfSessionsTomcat() {
		ObjectName name = new ObjectName("Tomcat:type=Manager,context=/streamr-core,host=localhost")
		(Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(name, "activeSessions")
	}

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
