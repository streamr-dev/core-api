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

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
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

	private ConcurrentMap<Long, ConcurrentMap<String, AtomicLong>> stats = new ConcurrentHashMap<>()

	private Timer flushTimer

	def increment(String metric, long userId, long count=1) {
		if (disabled || !metric || count < 1) { return }

		Map<String, AtomicLong> userStats = stats[userId]
		if (!userStats) {
			synchronized (stats) {
				stats.putIfAbsent(userId, new ConcurrentHashMap<String, AtomicLong>())
				userStats = stats[userId]
			}
		}

		AtomicLong counter = userStats[metric]
		if (!counter) {
			userStats.putIfAbsent(metric, new AtomicLong())
			counter = userStats[metric]
		}

		// Java 8 solution for the above
		//def userStats = stats.computeIfAbsent(userId, k -> new ConcurrentHashMap<String, AtomicLong>())
		//AtomicLong counter = userStats.computeIfAbsent(metric, k -> new AtomicLong())

		long newCount = counter.addAndGet(count)
		if (newCount > reportingPeriodEvents) {
			report(metric, userId)
		}
	}
	def increment(String metric, SecUser user, long count=1) {
		if (!user?.id) { return }
		increment(metric, user.id, count)
	}
	def increment(String metric, Stream stream, long count=1) {
		increment(metric, stream?.user, count)
	}

	private Lock flushLock = new ReentrantLock()
	def flush() {
		// if locked, bail; it's ok, someone else is flushing all metrics already
		if (flushLock.tryLock()) {
			try {
				List<Long> usersToRemove = new ArrayList<>()
				stats.each { Long userId, Map<String, AtomicLong> userStats ->
					long reportedValueSum = 0L
					userStats.each { String metric, AtomicLong counter ->
						reportedValueSum += report(metric, userId)
					}
					if (reportedValueSum == 0L) {
						usersToRemove.add(userId)
					}
				}

				// forget users that didn't have any measured activity in the last reporting interval
				usersToRemove.each { Long userId ->
					Map<String, AtomicLong> reportedStats
					synchronized (stats) {
						reportedStats = stats.remove(userId)
					}
					// double-check that stats were not incremented while reporting (or rather not reporting)
					reportedStats.each { String metric, AtomicLong counter ->
						long value = counter.get()
						if (value > 0L) {
							increment(metric, userId, value)	// restore the counter
						}
					}
				}
			} finally {
				flushLock.unlock()
			}
		}
	}

	@Synchronized
	private long report(String metric, long userId) {
		AtomicLong counter = stats[userId]?.get(metric)
		long value = counter?.get() ?: 0L
		if (value > 0L) {
			kafkaService.sendMessage(kafkaTopic, "", [
				metric: metric,
				user  : userId,
				value : value
			]);
			long justAdded = counter.getAndSet(0L) - value
			if (justAdded > 0L) {
				counter.addAndGet(justAdded)
			}
		}
		return value
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
