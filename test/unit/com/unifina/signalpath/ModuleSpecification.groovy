package com.unifina.signalpath

import com.unifina.utils.MockMetricsService
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class ModuleSpecification extends Specification {
	def setupSpec() {
		defineBeans {
			metricsService(MockMetricsService)
		}
	}
}
