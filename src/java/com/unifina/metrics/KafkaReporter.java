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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.unifina.domain.security.SecUser;
import com.unifina.service.KafkaService;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class KafkaReporter extends ScheduledReporter {
    private static final Logger log = Logger.getLogger(KafkaReporter.class);
	transient protected KafkaService kafkaService = null;

    private final String kafkaTopic;
    private final ObjectMapper mapper;
    private final MetricRegistry registry;
	protected final SecUser user;

    public KafkaReporter(MetricRegistry registry, String kafkaTopic, SecUser user) {
        super(registry, kafkaTopic, MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.SECONDS);
        this.registry = registry;
        this.mapper = new ObjectMapper().registerModule(new StreamrMetricsModule(user));
        this.kafkaTopic = kafkaTopic;
		this.user = user;
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
		try {
			StringWriter report = new StringWriter();
			mapper.writeValue(report, registry);
			kafkaService.sendMessage(kafkaTopic, "", report.toString());
			log.info("Metrics reported: " + report.toString());
		} catch (IOException e) {

		}
    }

	/** Jackson JSON serializer module */
	private static class StreamrMetricsModule extends MetricsModule {
		private final SecUser user;

		public StreamrMetricsModule(SecUser user) {
			super(TimeUnit.SECONDS, TimeUnit.SECONDS, false);
			this.user = user;
		}

		@Override
		public void setupModule(SetupContext context) {
			super.setupModule(context);
			context.addSerializers(new SimpleSerializers(Arrays.asList(new JsonSerializer<?>[] { new RegistrySerializer(user) })));
		}
	}

	/** Replaces MetricsModule serializer for MetricRegistry */
	private static class RegistrySerializer extends StdSerializer<MetricRegistry> {
		private final MetricFilter filter = MetricFilter.ALL;
		private final SecUser user;

		private RegistrySerializer(SecUser user) {
			super(MetricRegistry.class);
			this.user = user;
		}

		public void serialize(MetricRegistry registry, JsonGenerator json, SerializerProvider provider) throws IOException {
			json.writeStartObject();
			json.writeObjectField("user", user.getName());
			writeMeters(json, registry.getGauges(filter));
			writeMeters(json, registry.getCounters(filter));
			writeMeters(json, registry.getHistograms(filter));
			writeMeters(json, registry.getMeters(filter));
			writeMeters(json, registry.getTimers(filter));
			json.writeEndObject();
		}

		// flatten different meters into response object
		private void writeMeters(JsonGenerator json, SortedMap<String, ?> meters) throws IOException {
			for (Map.Entry<String, ?> entry : meters.entrySet()) {
				json.writeObjectField(entry.getKey(), entry.getValue());
			}
		}
	}
}
