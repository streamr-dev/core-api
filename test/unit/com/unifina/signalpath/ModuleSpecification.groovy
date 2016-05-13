package com.unifina.signalpath

import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.service.MetricsService
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class ModuleSpecification extends Specification {

	public static class MockMetricsService extends MetricsService {
		@Override public def increment(String metric, SecUser user, long count=0) { }
		@Override public def increment(String metric, long count=0) { }
		@Override public def increment(String metric, Stream stream, long count=0) { }
		@Override public def flush() { }
	}

	def setupSpec() {
		defineBeans {
			metricsService(MockMetricsService)
		}
	}
}
