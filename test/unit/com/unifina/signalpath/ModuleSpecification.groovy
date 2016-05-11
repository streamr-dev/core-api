package com.unifina.signalpath

import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class ModuleSpecification extends Specification {

	public static class MockMetricsService {
		public final int DEFAULT_REPORTING_PERIOD_SECONDS = 60
		public final int DEFAULT_REPORTING_PERIOD_EVENTS = 10000
		public String kafkaTopic = "streamr-metrics"
		public int reportingPeriodSeconds = DEFAULT_REPORTING_PERIOD_SECONDS
		public int reportingPeriodEvents = DEFAULT_REPORTING_PERIOD_EVENTS
		public Map<String, Map<SecUser, Long>> stats
		public def increment(String metric, SecUser user, long count=0) { }
		public def increment(String metric, long count=0) { }
		public def increment(String metric, Stream stream, long count=0) { }
		public def flush() { }
	}

	def setupSpec() {
		defineBeans {
			metricsService(MockMetricsService)
		}
	}
}
