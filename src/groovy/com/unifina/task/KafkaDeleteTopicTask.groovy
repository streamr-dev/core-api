package com.unifina.task;

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.task.Task
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
