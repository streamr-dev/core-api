package com.unifina.metrics;

import com.codahale.metrics.*;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unifina.domain.security.SecUser;
import com.unifina.service.KafkaService;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class KafkaReporter extends ScheduledReporter {
    private static final Logger log = Logger.getLogger(KafkaReporter.class);

	transient protected KafkaService kafkaService = null;

	private final MetricFilter filter;
    private final String kafkaTopic;
    private final ObjectMapper mapper;
    private final MetricRegistry registry;
	protected final SecUser user;

    public KafkaReporter(MetricRegistry registry, String kafkaTopic, SecUser user) {
        super(registry, kafkaTopic, MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.SECONDS);
        this.registry = registry;
		this.mapper = new ObjectMapper().registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, false));
        this.kafkaTopic = kafkaTopic;
		this.user = user;
		this.filter = MetricFilter.ALL;
    }

    @Override
    public synchronized void report(SortedMap<String, Gauge> gauges,
                                    SortedMap<String, Counter> counters,
                                    SortedMap<String, Histogram> histograms,
                                    SortedMap<String, Meter> meters,
                                    SortedMap<String, Timer> timers) {
		if (kafkaService == null) {
			kafkaService = (KafkaService) Holders.getGrailsApplication().getMainContext().getBean("kafkaService");
		}
		sendMetrics(registry.getGauges(filter));
		sendMetrics(registry.getCounters(filter));
		sendMetrics(registry.getHistograms(filter));
		sendMetrics(registry.getMeters(filter));
		sendMetrics(registry.getTimers(filter));
    }

	private synchronized void sendMetrics(Map<String, ?> metrics) {
		for (Map.Entry<String, ?> metric : metrics.entrySet()) {
			try {
				Map<String, Object> message = new LinkedHashMap<>();
				message.put("user", user.getName());
				message.put("metric", metric.getKey());
				message.put("value", metric.getValue());

				StringWriter json = new StringWriter();
				mapper.writeValue(json, message);
				kafkaService.sendMessage(kafkaTopic, "", json.toString());
			} catch (IOException e) {
				log.info("Write failed for metric " + metric.getKey());
			}
		}
	}
}
