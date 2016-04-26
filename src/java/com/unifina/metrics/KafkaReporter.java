/**
 * Adapted from https://github.com/stealthly/metrics-kafka/blob/master/codahale/src/main/java/ly/stealth/kafka/metrics/KafkaReporter.java
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	private synchronized void sendMetrics(SortedMap<String, ?> metrics) {
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
