package com.unifina.utils

import com.codahale.metrics.Histogram
import com.codahale.metrics.Meter
import com.codahale.metrics.Timer
import com.unifina.service.MetricsService

class MockMetricsService extends MetricsService {

	@Override
	void afterPropertiesSet() throws Exception { }

	@Override
	Meter getMeterFor(String metricName, Object host=null) {
		return new Meter() {
			@Override
			public void mark(long n) { }
		}
	}

	@Override
	Histogram getHistogramFor(String metricName, Object host=null) {
		return new Histogram() {
			@Override
			public void mark(long n) { }
		}
	}

	@Override
	Timer getTimerFor(String metricName, Object host=null) {
		return new Timer() {
			@Override
			public void mark(long n) { }
		}
	}
}
