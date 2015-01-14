package com.unifina.service

import grails.converters.JSON
import groovy.transform.CompileStatic
import kafka.admin.AdminUtils
import kafka.javaapi.TopicMetadata
import kafka.javaapi.TopicMetadataRequest
import kafka.javaapi.TopicMetadataResponse
import kafka.javaapi.consumer.SimpleConsumer
import kafka.producer.ProducerConfig
import kafka.utils.ZKStringSerializer

import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.exception.ZkMarshallingError
import org.I0Itec.zkclient.serialize.ZkSerializer
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.kafkaclient.UnifinaKafkaConsumer
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.kafkaclient.UnifinaKafkaMessageHandler
import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.task.KafkaCollectTask
import com.unifina.task.KafkaDeleteTopicTask
import com.unifina.utils.TimeOfDayUtil


class KafkaService {

	GrailsApplication grailsApplication
	
	UnifinaKafkaProducer producer = null
	SimpleConsumer topicCreateConsumer = null
	
	private static final Logger log = Logger.getLogger(KafkaService)
	
	@CompileStatic
	private Properties getProperties() {
		return ((ConfigObject)grailsApplication.config["unifina"]["kafka"]).toProperties()
	}
	
	@CompileStatic
	UnifinaKafkaProducer getProducer() {
		if (producer == null) {
			Properties props = getProperties()
			ProducerConfig producerConfig = new ProducerConfig(props)
			producer = new UnifinaKafkaProducer(props)
		}
		return producer
	}
	
	@CompileStatic
	private ZkClient createZkClient() {
		// serializer must be explicitly given due to https://issues.apache.org/jira/browse/KAFKA-1737
		return new ZkClient(getProperties().getProperty("zookeeper.connect"), 30000, 30000, new ZKSerializerImpl());
	}
	
	/**
	 * Creates a SimpleConsumer that is only used for sending FetchMetadataRequests
	 * when topics are created.
	 * @return
	 */
	@CompileStatic
	private SimpleConsumer getTopicCreateConsumer() {
		if (topicCreateConsumer==null) {
			Properties props = getProperties()
			String[] brokers = props.getProperty("metadata.broker.list").split(",")
			
			topicCreateConsumer = new SimpleConsumer(brokers[0].split(":")[0], Integer.parseInt(brokers[0].split(":")[1]), 2000, 1024*1024, "TopicCreateConsumer");
		}
		
		return topicCreateConsumer
	}
	
	@CompileStatic
    void sendMessage(String channelId, Object key, String message, boolean isJson=true) {
		if (isJson)
			getProducer().sendJSON(channelId, key.toString(), System.currentTimeMillis(), message)
		else getProducer().sendString(channelId, key.toString(), System.currentTimeMillis(), message)
    }
	
	@CompileStatic
	void sendMessage(String channelId, Object key, Map message) {
		String str = (message as JSON).toString();
		sendMessage(channelId, key, str, true);
	}
	
	/**
	 * Uses a FetchMetadataRequest to create topics. This requires
	 * auto.create.topics.enable=true on the server. This is a workaround for
	 * AdminUtils.createTopic not seeming to work properly on Kafka 0.8.1.1 and 0.8.2-beta.
	 * @param topics
	 */
	@CompileStatic
	void createTopics(List<String> topics) {
		SimpleConsumer consumer = getTopicCreateConsumer();
		
		TopicMetadataRequest req = new TopicMetadataRequest(topics);
		
		int retry = 0;
		boolean success = false;
		while (retry++ < 20) {
			
			log.info("createTopics: sending TopicMetadataRequest for "+topics)
			TopicMetadataResponse resp = consumer.send(req);
			
			boolean hadError = false
			for (TopicMetadata tmd : resp.topicsMetadata()) {
				if (tmd.errorCode()>0) {
					hadError = true
				}
			}
			
			if (hadError) {
				log.info("createTopics: Retrying topic metadata fetch for "+topics+", retry "+retry);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
			else {
				success = true
				break
			}
		}
		
		if (!success) {
			throw new RuntimeException("Failed to create topics: "+topics)
		}
	}
	
	/**
	 * Immediately marks topics for deletion. For delayed delete, see createDeleteTopicTask()
	 * @param topics
	 */
	@CompileStatic
	void deleteTopics(List topics) {
		ZkClient zkClient = createZkClient()
		
		for (String topic : topics) {
			try {
				AdminUtils.deleteTopic(zkClient, topic)
			} catch (Exception e) {
				log.warn("Failed to delete topic "+topic+", due to: "+e.getMessage());
			}
		}
		
		zkClient.close()
	}
	
	/**
	 * Creates and saves a delayed Task for the deletion of topics
	 * @param topics
	 * @param delayMs
	 */
	void createDeleteTopicTask(List topics, long delayMs) {
		Map config = KafkaDeleteTopicTask.getConfig(topics)
		Task task = new Task(KafkaDeleteTopicTask.class.getName(), (config as JSON).toString(), "kafka-delete", UUID.randomUUID().toString())
		task.runAfter = new Date(System.currentTimeMillis() + delayMs)
		task.save()
	}
	
	Date getFirstTimestamp(String topic) {
		log.info("Querying first timestamp for topic $topic...")
		UnifinaKafkaConsumer consumer = new UnifinaKafkaConsumer(getProperties())
		Date firstTimestamp = null
		consumer.subscribe(topic, new UnifinaKafkaMessageHandler() {
			@Override
			public void handleMessage(UnifinaKafkaMessage msg) {
				if (firstTimestamp==null)
					firstTimestamp = new Date(msg.getTimestamp())
			}
		}, true)
		
		// Wait for the Kafka consumption to finish (or timeout)!
		while (firstTimestamp==null && consumer.getTimeSinceLastEvent() < 60L*1000L) {
			Thread.sleep(1000L);
		}
		consumer.close()
		
		if (firstTimestamp==null)
			log.warn("Timed out while waiting for the first message of topic $topic")
		
		return firstTimestamp
	}
	
	List<Task> createCollectTasks(Stream stream) {
		// The latest FeedFile indicates the last collected day
		FeedFile latest = FeedFile.withCriteria(uniqueResult:true) {
			eq("stream",stream)
			maxResults(1)
			order("endDate", "desc")
		}
		
		Date beginDate
		Date endDate
		if (latest) {
			beginDate = latest.beginDate+1
			endDate = latest.endDate+1
		}
		// If never collected, query the first timestamp from Kafka
		else {
			Map streamConfig = JSON.parse(stream.streamConfig)
			String topic = streamConfig.topic

			// If getFirstTimestamp(topic) returns null, there is nothing to be collected
			beginDate = getFirstTimestamp(topic)
			if (beginDate==null) {
				log.warn("Could not determine first timestamp for stream $stream.name, not collecting")
				return []
			}
			beginDate = TimeOfDayUtil.getMidnight(beginDate)
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
	
	@CompileStatic
	class ZKSerializerImpl implements ZkSerializer {

		@Override
			public Object deserialize(byte[] b) throws ZkMarshallingError {
			return ZKStringSerializer.deserialize(b);
		}
		@Override
			public byte[] serialize(Object o) throws ZkMarshallingError {
			return ZKStringSerializer.serialize(o);
		}
		
	}
		
}
