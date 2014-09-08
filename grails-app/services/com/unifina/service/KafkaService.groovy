package com.unifina.service

import java.util.Date;

import grails.converters.JSON
import groovy.transform.CompileStatic
import kafka.producer.ProducerConfig

import org.apache.catalina.util.DateTool;
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser;
import com.unifina.domain.task.Task
import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.task.KafkaCollectTask
import com.unifina.utils.TimeOfDayUtil;


class KafkaService {

	UnifinaKafkaProducer producer = null
	GrailsApplication grailsApplication

	@CompileStatic
    void sendMessage(String channelId, Object key, String message, boolean isJson=true) {
		if (producer == null) {
			Properties props = ((ConfigObject)grailsApplication.config["unifina"]["kafka"]).toProperties()
			ProducerConfig producerConfig = new ProducerConfig(props)
			producer = new UnifinaKafkaProducer(props)
		}

		if (isJson)
			producer.sendJSON(channelId, key.toString(), System.currentTimeMillis(), message)
		else producer.sendString(channelId, key.toString(), System.currentTimeMillis(), message)
    }
	
	@CompileStatic
	void sendMessage(String channelId, Object key, Map message) {
		String str = (message as JSON).toString();
		sendMessage(channelId, key, str, true);
	}
	
	List<Task> createCollectTasks(Stream stream) {
		// The latest FeedFile indicates the last collected day
		FeedFile latest = FeedFile.withCriteria(uniqueResult:true) {
			eq("feed",stream.feed)
			maxResults(1)
			order("endDate", "desc")
		}
		
		Date beginDate
		Date endDate
		if (latest) {
			beginDate = latest.beginDate+1
			endDate = latest.endDate+1
		}
		// If never collected, use yesterday
		// TODO: query from Kafka what is the first day?
		else {
			beginDate = TimeOfDayUtil.getMidnight(new Date())-1
			endDate = new Date((beginDate+1).time-1)
		}
	
		// Create the task for each day up to today
		Date limit = TimeOfDayUtil.getMidnight(new Date())
		
		List tasks = []
		while (endDate.before(limit)) {
			Map config = KafkaCollectTask.getConfig(stream, beginDate, endDate)
			String configString = (config as JSON)
			
			// Check that the task does not exist already
			if (!Task.findByImplementingClassAndConfig(KafkaCollectTask.class.getName(), configString)) {
				Task task = new Task()
				task.available = true
				task.complete = false
				task.complexity = 0	
				task.category = "kafka-collect"
				task.config = (config as JSON).toString()
				task.implementingClass = KafkaCollectTask.class.name
				task.taskGroupId = UUID.randomUUID().toString()
				task.save(failOnError:true)
				tasks << task
			}
			beginDate += 1
			endDate += 1
		}
		return tasks
		
	}
}
