package com.unifina.service

import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

@CompileStatic
class MetricsService implements InitializingBean, DisposableBean {
	static transactional = false

	SpringSecurityService springSecurityService
	KafkaService kafkaService

	final int DEFAULT_REPORTING_PERIOD_SECONDS = System.getProperty("metrics.reportingPeriodSeconds")?.toInteger() ?: 60
	final int DEFAULT_REPORTING_PERIOD_EVENTS = System.getProperty("metrics.reportingPeriodEvents")?.toInteger() ?: 10000
	final boolean isActive = System.getProperty("metrics.isActive")?.toBoolean() ?: true

	String kafkaTopic = "streamr-metrics"

	int reportingPeriodEvents = DEFAULT_REPORTING_PERIOD_EVENTS
	int reportingPeriodSeconds = DEFAULT_REPORTING_PERIOD_SECONDS

	private Map<String, Map<Long, AtomicLong>> stats

	private Timer flushTimer

	private final incrementInitLock = new Object[0]
	def increment(String metric, SecUser user, long count=1) {
		if (!isActive || !metric || !user?.id || count < 1) { return }
		long userId = user.id

		AtomicLong counter
		synchronized (incrementInitLock) {
			if (!stats) { stats = [:] }
			def m = stats[metric]
			if (!m) { m = stats[metric] = [:] }
			counter = m[userId]
			if (!counter) { counter = m[userId] = new AtomicLong() }
		}

		long newCount = counter.addAndGet(count)
		if (newCount > reportingPeriodEvents) {
			report(metric, userId)
		}
	}
	def increment(String metric, Stream stream, long count=1) {
		increment(metric, stream?.user, count)
	}

	private Lock flushLock = new ReentrantLock()
	def flush() {
		// if locked, bail; it's ok, someone else is reporting this metric already
		if (stats && flushLock.tryLock()) {
			try {
				stats.each { String metric, Map<Long, AtomicLong> mStats ->
					mStats.each { Long userId, AtomicLong counter ->
						report(metric, userId as long)
					}
				}
				stats.clear()
			} finally {
				flushLock.unlock()
			}
		}
	}

	@Synchronized
	private def report(String metric, long userId) {
		kafkaService.sendMessage(kafkaTopic, "", [
			metric: metric,
			user: userId,
			value: stats[metric][userId].get()
		]);

		// zero it for now, clean up all after flush
		stats[metric][userId] = new AtomicLong()
	}

	@Synchronized
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
