package com.unifina.task

import com.unifina.service.KafkaService
import grails.converters.JSON
import groovy.transform.CompileStatic

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.json.JSONObject

import com.unifina.kafkaclient.UnifinaKafkaConsumer
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.kafkaclient.UnifinaKafkaMessageHandler
import com.unifina.utils.MapTraversal

@CompileStatic
class TaskMessageListener implements UnifinaKafkaMessageHandler {
	
	GrailsApplication grailsApplication
	UnifinaKafkaConsumer consumer
	List<TaskWorker> localTaskWorkers

	boolean quit = false
	
	public static final Logger log = Logger.getLogger(TaskMessageListener.class)
	
	public TaskMessageListener(GrailsApplication grailsApplication, List<TaskWorker> localTaskWorkers) {
		this.grailsApplication = grailsApplication
		this.localTaskWorkers = localTaskWorkers
		
		Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(grailsApplication.config, "unifina.kafka"));
		Properties properties = new Properties();
		for (String s : kafkaConfig.keySet())
			properties.setProperty(s, kafkaConfig.get(s).toString());

		String taskTopic = MapTraversal.getString(grailsApplication.config, "unifina.task.messageQueue")

		// Make sure the task topic exists
		KafkaService kafkaService = (KafkaService) grailsApplication.mainContext.getBean("kafkaService")
		kafkaService.createTopics([taskTopic])

		consumer = new UnifinaKafkaConsumer(properties);
		consumer.subscribe(taskTopic,this)
	}

	public void quit() {
		consumer.close()
	}

	@Override
	public void handleMessage(UnifinaKafkaMessage msg, String topic, int partition, long offset) {
		Map json = (JSONObject) JSON.parse(msg.toString())
		if (json.type=="abort") {
			def id = json.id
			log.info("Abort message received for task $id")
			// Is any of the local TaskWorkers running a Task by this id?
			localTaskWorkers.each {TaskWorker tw->
				if (tw.currentTask?.id==id) {
					tw.abort()
				}
			}
		}
		else log.warn("Unknown type of task message received: "+msg)
	}

}
