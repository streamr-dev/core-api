package com.unifina.service

import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.Histogram
import com.codahale.metrics.Meter
import com.codahale.metrics.Timer
import com.codahale.metrics.MetricRegistry
import static com.codahale.metrics.MetricRegistry.name;
import com.unifina.metrics.KafkaReporter
import grails.transaction.Transactional
import org.springframework.beans.factory.InitializingBean

import java.util.concurrent.TimeUnit

@Transactional
class MetricsService implements InitializingBean {

	def springSecurityService

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
			kafkaReporter = new KafkaReporter(metrics, "streamr-metrics", springSecurityService.getCurrentUser())
		}
		kafkaReporter.start(interval, TimeUnit.SECONDS)
	}

	Meter getMeterFor(String metricName, Object host=null) { return metrics.meter(name(host?.class, metricName)) }
	Histogram getHistogramFor(String metricName, Object host=null) { return metrics.histogram(name(host?.class, metricName)) }
	Timer getTimerFor(String metricName, Object host=null) { return metrics.timer(name(host?.class, metricName)) }
}
