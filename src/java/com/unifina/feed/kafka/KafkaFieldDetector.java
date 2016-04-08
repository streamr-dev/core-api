package com.unifina.feed.kafka;

import com.unifina.domain.data.Stream;
import com.unifina.feed.FieldDetector;
import com.unifina.feed.map.MapMessage;
import com.unifina.kafkaclient.KafkaOffsetUtil;
import com.unifina.kafkaclient.UnifinaKafkaMessage;
import com.unifina.service.KafkaService;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class KafkaFieldDetector extends FieldDetector {
	private final KafkaService kafkaService;

	public KafkaFieldDetector(GrailsApplication grailsApplication) {
		super(grailsApplication);
		this.kafkaService = (KafkaService) grailsApplication.getMainContext().getBean("kafkaService");
	}

	@Override
	protected MapMessage fetchExampleMessage(Stream stream) {
		KafkaOffsetUtil util = kafkaService.getOffsetUtil();

		KafkaConsumer<byte[], byte[]> c = util.getNewConsumer();
		List<PartitionInfo> partitions = c.partitionsFor(stream.getId());

		// Find the latest message over all partitions
		UnifinaKafkaMessage latestMessage = null;
		for (PartitionInfo pi : partitions) {
			long offset = util.getLastOffset(stream.getId(), pi.partition());
			if (offset > 0) {
				UnifinaKafkaMessage msg = util.getMessage(stream.getId(), pi.partition(), offset - 1);
				if (msg != null && (latestMessage == null || msg.getTimestamp() > latestMessage.getTimestamp()))
					latestMessage = msg;
			}
		}

		// Also closes the KafkaConsumer c
		util.close();

		if (latestMessage==null)
			return new MapMessage(null, null, new HashMap());
		else {
			KafkaMessage msg = new KafkaMessageParser().parse(latestMessage);
			return new MapMessage(msg.getTimestamp(), msg.getTimestamp(), msg.payload);
		}
	}
}
