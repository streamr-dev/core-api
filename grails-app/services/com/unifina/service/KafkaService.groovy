package com.unifina.service

import grails.converters.JSON
import groovy.transform.CompileStatic
import kafka.admin.AdminUtils
import kafka.javaapi.consumer.SimpleConsumer
import kafka.producer.ProducerConfig
import kafka.utils.ZKStringSerializer
import kafka.utils.ZkUtils

import org.I0Itec.zkclient.ZkClient
import org.I0Itec.zkclient.ZkConnection
import org.I0Itec.zkclient.exception.ZkMarshallingError
import org.I0Itec.zkclient.serialize.ZkSerializer
import org.apache.commons.lang.time.DateUtils
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.kafka.KafkaFeedFileName
import com.unifina.feed.kafka.KafkaFeedFileWriter
import com.unifina.kafkaclient.KafkaOffsetUtil
import com.unifina.kafkaclient.UnifinaKafkaConsumer
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.kafkaclient.UnifinaKafkaMessageHandler
import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.task.KafkaCollectTask
import com.unifina.task.KafkaDeleteTopicTask
import com.unifina.utils.CSVImporter
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
			producer = new UnifinaKafkaProducer(props)
		}
		return producer
	}

	@CompileStatic KafkaOffsetUtil getOffsetUtil() {
		return new KafkaOffsetUtil(getProperties())
	}
	
	@CompileStatic
	private ZkUtils createZkUtils() {
		// serializer must be explicitly given due to https://issues.apache.org/jira/browse/KAFKA-1737
		ZkClient zkClient = new ZkClient(getProperties().getProperty("zookeeper.connect"), 30000, 30000, new ZKSerializerImpl());
		ZkConnection zkConnection = new ZkConnection(getProperties().getProperty("zookeeper.connect"))
		ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false)
		return zkUtils
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
	
	@CompileStatic
	void sendMessage(Stream stream, Object key, Map message) {
		String str = (message as JSON).toString();
		sendMessage(stream.getId(), key, str, true);
	}
	
	@CompileStatic
	void createTopics(List<String> topics, int partitions=1, int replicationFactor=1) {
		ZkUtils zkUtils = createZkUtils()
		
		for (String topic : topics) {
			if (AdminUtils.topicExists(zkUtils, topic)) {
				log.warn("createTopics: topic $topic already exists")
			}
			else {
				Properties props = new Properties();
				AdminUtils.createTopic(zkUtils, topic, partitions, replicationFactor, props);
			}
		}
		
		zkUtils.close()
	}
	
	@CompileStatic
	boolean topicExists(String topic) {
		ZkUtils zkUtils = createZkUtils()
		boolean result = AdminUtils.topicExists(zkUtils, topic)
		zkUtils.close()
		return result
	}
	
	/**
	 * Immediately marks topics for deletion. For delayed delete, see createDeleteTopicTask()
	 * @param topics
	 */
	@CompileStatic
	void deleteTopics(List topics) {
		ZkUtils zkUtils = createZkUtils()
		
		for (String topic : topics) {
			try {
				AdminUtils.deleteTopic(zkUtils, topic)
			} catch (Exception e) {
				log.warn("Failed to delete topic "+topic+", due to: "+e.getMessage());
			}
		}
		
		zkUtils.close()
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
	
	@CompileStatic
	Date getFirstTimestamp(String topic) {
		KafkaOffsetUtil util = new KafkaOffsetUtil(getProperties())
		Date result = util.getFirstTimestamp(topic)
		util.close()
		return result
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
			// If getFirstTimestamp(topic) returns null, there is nothing to be collected
			beginDate = getFirstTimestamp(stream.id)
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
	public List<FeedFile> createFeedFilesFromCsv(CSVImporter csv, Stream stream, FeedFileService feedFileService=null) {
		
		List<KafkaFeedFileWriter> doneWriters = []
		List<Date> beginDates = []
		List<Date> endDates = []
		
		KafkaFeedFileWriter writer
		Date prevDate
		
		// Don't list FeedFileService as injected dependency because of a circular reference situation
		if (!feedFileService)
			feedFileService = (FeedFileService) grailsApplication.getMainContext().getBean("feedFileService")
		
		for (CSVImporter.LineValues line : csv) {
			Date date = line.getTimestamp()
			
			// Check that we have a writer for the current day
			if (writer==null || !DateUtils.isSameDay(date, prevDate)) {
				if (writer!=null) {
					println "Done with writer for $prevDate"
					writer.close()
					doneWriters.add(writer)
					endDates.add(prevDate)
				}
				
				Date beginDate = TimeOfDayUtil.getMidnight(date)
				String filename = new KafkaFeedFileName(stream, beginDate).toString()
				writer = new KafkaFeedFileWriter(filename)
				beginDates.add(beginDate)
			}
			
			// Take note of previous date for the next round
			prevDate = date
			
			// Write all fields into the item except for the timestamp column
			Map item = [:]
			for (int i=0; i<line.values.length; i++) {
				if (i!=line.schema.timestampColumnIndex && line.values[i]!=null) {
					String name = line.schema.entries[i].name
					item[name] = line.values[i]
				}
			}
			
			// Create JSON stringification
			String data = (item as JSON).toString()
			
			// Write to file
			UnifinaKafkaMessage msg = new UnifinaKafkaMessage(null, null, date.time, UnifinaKafkaMessage.CONTENT_TYPE_JSON, data.getBytes("UTF-8"))
			writer.write(msg.toBytes())
		}
		
		// Close the last writer
		if (writer!=null) {
			writer.close()
			doneWriters.add(writer)
			endDates.add(prevDate)
		}
		
		List<FeedFile> result = []
		// Create the FeedFiles
		for (int i=0;i<doneWriters.size();i++) {
			// Create the FeedFile (not overwriting existing)
			FeedFile feedFile = feedFileService.createFeedFile(stream, beginDates[i], endDates[i], doneWriters[i].getFile(), false)
			if (feedFile!=null)
				result.add(feedFile)
			
			// Delete the temporary file
			doneWriters[i].deleteFile()
		}
		
		return result
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
