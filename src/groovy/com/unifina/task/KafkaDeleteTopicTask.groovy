package com.unifina.task;

import grails.converters.JSON

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.zip.GZIPOutputStream

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.kafka.KafkaFeedFileName
import com.unifina.kafkaclient.UnifinaKafkaChannelConsumer
import com.unifina.kafkaclient.UnifinaKafkaConsumer
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.kafkaclient.UnifinaKafkaMessageHandler
import com.unifina.kafkaclient.UnifinaKafkaUtils;
import com.unifina.service.KafkaService

/**
 * Deletes a topic from Kafka
 */
public class KafkaDeleteTopicTask extends AbstractTask {

	private KafkaService kafkaService;

	private static final Logger log = Logger.getLogger(KafkaDeleteTopicTask)
	
	public KafkaDeleteTopicTask(Task task, Map<String, Object> config,
			GrailsApplication grailsApplication) {
		super(task, config, grailsApplication);
		kafkaService = (KafkaService) grailsApplication.getMainContext().getBean("kafkaService");
	}

	@Override
	public boolean run() {
		List topics = config.topics
		kafkaService.deleteTopics(topics)
		return true
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
	public static Map<String,Object> getConfig(List topics) {
		return [topics:topics]
	}
}
