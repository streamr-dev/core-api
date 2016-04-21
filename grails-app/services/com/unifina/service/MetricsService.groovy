package com.unifina.service

import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import grails.transaction.Transactional
import org.springframework.beans.factory.InitializingBean

import java.util.concurrent.TimeUnit

@Transactional
class MetricsService implements InitializingBean {

	public MetricRegistry metrics = new MetricRegistry();

	@Override
	void afterPropertiesSet() throws Exception {
		startConsoleReporter()
	}

	def startConsoleReporter() {
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
			.convertRatesTo(TimeUnit.SECONDS)
			.convertDurationsTo(TimeUnit.MILLISECONDS)
			.build();
		reporter.start(5, TimeUnit.SECONDS);
    }

	public Meter getMeter(String metricName) {
		return metrics.meter(metricName);
	}
}
