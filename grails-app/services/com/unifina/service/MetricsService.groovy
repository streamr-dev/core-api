package com.unifina.service

import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.Histogram
import com.codahale.metrics.Meter
import com.codahale.metrics.Timer
import com.codahale.metrics.MetricRegistry
import com.unifina.metrics.KafkaReporter
import grails.transaction.Transactional
import org.springframework.beans.factory.InitializingBean

import java.util.concurrent.TimeUnit

@Transactional
class MetricsService implements InitializingBean {

	final DEFAULT_REPORT_INTERVAL_SECONDS = 5
	public MetricRegistry metrics = new MetricRegistry();
	private KafkaReporter kafkaReporter;

	@Override
	void afterPropertiesSet() throws Exception {
		//startConsoleReporter()
		startKafkaReporter()
	}

	def startConsoleReporter(int interval=DEFAULT_REPORT_INTERVAL_SECONDS) {
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
			.convertRatesTo(TimeUnit.SECONDS)
			.convertDurationsTo(TimeUnit.MILLISECONDS)
			.build()
		reporter.start(interval, TimeUnit.SECONDS)
    }

	def startKafkaReporter(int interval=DEFAULT_REPORT_INTERVAL_SECONDS) {
		if (kafkaReporter) {
			kafkaReporter.stop()
		} else {
			kafkaReporter = new KafkaReporter(metrics, "streamr-metrics")
		}
		kafkaReporter.start(interval, TimeUnit.SECONDS)
	}

	Meter getMeterFor(String metricName) { return metrics.meter(metricName) }
	Histogram getHistogramFor(String metricName) { return metrics.histogram(metricName) }
	Timer getTimerFor(String metricName) { return metrics.timer(metricName) }
}
