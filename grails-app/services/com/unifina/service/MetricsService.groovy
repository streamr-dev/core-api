package com.unifina.service

import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.utils.MapTraversal
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
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

	final int DEFAULT_REPORTING_PERIOD_SECONDS = MapTraversal.getInt(Holders.config, "unifina.metrics.reportingPeriodSeconds", 60)
	final int DEFAULT_REPORTING_PERIOD_EVENTS = MapTraversal.getInt(Holders.config, "unifina.metrics.reportingPeriodEvents", 10000)
	private boolean disabled = MapTraversal.getBoolean(Holders.config, "unifina.metrics.disabled")

	String kafkaTopic = MapTraversal.getString(Holders.config, "unifina.metrics.kafkaTopic", "streamr-metrics")

	int reportingPeriodEvents = DEFAULT_REPORTING_PERIOD_EVENTS
	int reportingPeriodSeconds = DEFAULT_REPORTING_PERIOD_SECONDS

	private Map<String, Map<Long, AtomicLong>> stats = [:]
	// See Java 8 solution for increment
	//private ConcurrentMap<String, ConcurrentMap<Long, AtomicLong>> stats = new ConcurrentHashMap<>()

	private Timer flushTimer

	private final incrementInitLock = new Object[0]
	def increment(String metric, SecUser user, long count=1) {
		if (disabled || !metric || !user?.id || count < 1) { return }
		long userId = user.id

		AtomicLong counter
		synchronized (incrementInitLock) {
			def m = stats[metric]
			if (!m) { m = stats[metric] = [:] }
			counter = m[userId]
			if (!counter) { counter = m[userId] = new AtomicLong() }
		}

		// Java 8 solution for the above, no need for manual locking
		//def m = stats.computeIfAbsent(metric, k -> new ConcurrentHashMap<Long, AtomicLong>()) ?: stats.get(metric)
		//AtomicLong counter = m.computeIfAbsent(userId, k -> new AtomicLong()) ?: m.get(userId)

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
		// if locked, bail; it's ok, someone else is flushing all metrics already
		if (flushLock.tryLock()) {
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

		// zero it for now, clean up all metrics after flush
		stats[metric][userId].set(0L)
	}

	@Synchronized
	void setReportingPeriodSeconds(int t) {
		if (flushTimer) { flushTimer.cancel() }
		flushTimer = new Timer("MetricsService");
		flushTimer.schedule(new TimerTask() {
			@Override
			void run() {
				flush()
			}
		}, 0, reportingPeriodSeconds*1000);
	}

	boolean isDisabled() { disabled }
	void disable() {
		if (disabled) { return }
		disabled = true
		if (flushTimer) { flushTimer.cancel() }
		flush()
	}
	void enable() {
		if (!disabled) { return }
		disabled = false
		setReportingPeriodSeconds(reportingPeriodSeconds)
	}

	@Override
	void afterPropertiesSet() throws Exception {
		// start thread
		if (!disabled) {
			setReportingPeriodSeconds(reportingPeriodSeconds)
		}
	}

	@Override
	void destroy() throws Exception {
		// stop thread and flush
		disable()
	}
}
