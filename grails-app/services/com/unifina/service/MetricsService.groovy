package com.unifina.service

import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import grails.transaction.Transactional
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean

@Transactional
class MetricsService implements InitializingBean, DisposableBean {

	def springSecurityService
	def kafkaService

	final int DEFAULT_REPORTING_PERIOD_SECONDS = System.getProperty("metrics.reportingPeriodSeconds")?.toInteger() ?: 60
	final int DEFAULT_REPORTING_PERIOD_EVENTS = System.getProperty("metrics.reportingPeriodEvents")?.toInteger() ?: 10000
	final boolean isActive = System.getProperty("metrics.isActive")?.toBoolean() ?: true

	String kafkaTopic = "streamr-metrics"

	int reportingPeriodEvents = DEFAULT_REPORTING_PERIOD_EVENTS
	int reportingPeriodSeconds = DEFAULT_REPORTING_PERIOD_SECONDS

	private Map<String, Map<SecUser, Long>> stats

	private Timer flushTimer

	def increment(String metric, SecUser user, long count=1) {
		if (!isActive || !metric || !user || count < 1) { return }

		if (!stats) { stats = [:] }
		def m = stats[metric]
		if (!m) { m = stats[metric] = [:] }

		long newCount = (m[user] ?: 0L) + count
		m[user] = newCount
		if (newCount > reportingPeriodEvents) {
			report(metric, user)
		}
	}
	def increment(String metric, long count=1) {
		increment(metric, (SecUser)springSecurityService.getCurrentUser(), count)
	}
	def increment(String metric, Stream stream, long count=1) {
		increment(metric, stream?.user, count)
	}

	def flush() {
		stats.each { metric, mStats ->
			mStats.each { user, count ->
				report(metric, user)
			}
		}
	}

	private def report(String metric, SecUser user) {
		kafkaService.sendMessage(kafkaTopic, "", [
			metric: metric,
			user: user.id,
			value: stats[metric][user]
		]);

		// clean up after sending to avoid accumulating counters over time
		stats[metric].remove(user)
		if (stats[metric].size < 1) {
			stats.remove(metric)
		}
	}

	void setReportingPeriodSeconds(int t) {
		if (flushTimer) {
			flushTimer.cancel()
		}
		flushTimer = new Timer("MetricsService");
		flushTimer.schedule(new TimerTask() {
			@Override
			void run() {
				flush()
			}
		}, 0, reportingPeriodSeconds*1000);
	}

	@Override
	void afterPropertiesSet() throws Exception {
		// start thread
		setReportingPeriodSeconds(reportingPeriodSeconds)
	}

	@Override
	void destroy() throws Exception {
		// stop thread
		flushTimer.cancel()
		flush()
	}
}
