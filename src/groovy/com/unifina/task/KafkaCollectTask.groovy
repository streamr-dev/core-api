package com.unifina.task;

import grails.converters.JSON

import java.text.SimpleDateFormat
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.kafka.KafkaFeedFileName
import com.unifina.feed.kafka.KafkaFeedFileWriter
import com.unifina.kafkaclient.UnifinaKafkaConsumer
import com.unifina.kafkaclient.UnifinaKafkaIterator;
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.kafkaclient.UnifinaKafkaMessageHandler
import com.unifina.service.FeedFileService

/**
 * Collects data from Kafka for a given Stream, sends the file to
 * file storage and writes a corresponding FeedFile entry.
 */
public class KafkaCollectTask extends AbstractTask {

	// Package private for easier unit testing
	FeedFileService feedFileService;

	private static final Logger log = Logger.getLogger(KafkaCollectTask)
	
	public KafkaCollectTask(Task task, Map<String, Object> config,
			GrailsApplication grailsApplication) {
		super(task, config, grailsApplication);
		feedFileService = (FeedFileService) grailsApplication.getMainContext().getBean("feedFileService");
	}

	@Override
	public boolean run() {
		Stream stream = Stream.get(config.streamId)
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
		df.setTimeZone(TimeZone.getTimeZone("UTC"))
		
		final long beginTime = (config.beginDate instanceof String ? df.parse(config.beginDate).time : config.beginDate)
		final long endTime = (config.endDate instanceof String ? df.parse(config.endDate).time : config.endDate)
		String beginTimeAsString = new Date(beginTime).toString()
		String topic = stream.id
		
		String name = config.filename
		
		final KafkaFeedFileWriter writer = createWriter(name)
		
		int counter = 0

		UnifinaKafkaIterator iterator = createIterator(topic, new Date(beginTime), new Date(endTime))
		try {
			while (iterator.hasNext()) {
				UnifinaKafkaMessage msg = iterator.next()
				counter++
				writer.write(msg.toBytes())
			}
		} 
		catch (IOException e) {
			log.error("Error writing to file!",e)
		} 
		finally {
			iterator.close()
		}
		
		writer.close()
		
		log.info("Done, found $counter messages for $topic.")
		
		feedFileService.createFeedFile(stream, new Date(beginTime), new Date(endTime), writer.getFile(), true)
		
		// Clean up the temp files
		writer.deleteFile()

		return true
	}
	
	protected UnifinaKafkaIterator createIterator(String topic, Date from, Date to) {
		new UnifinaKafkaIterator(topic, from, to, 30000L, grailsApplication.config.unifina.kafka.toProperties())
	}
	
	protected KafkaFeedFileWriter createWriter(String name) {
		new KafkaFeedFileWriter(name)
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
	public static Map<String,Object> getConfig(Stream stream, Date beginDate, Date endDate) {
		String filename = new KafkaFeedFileName(stream, beginDate).toString()
		return [streamId:stream.id, beginDate:beginDate.time, endDate:endDate.time, filename:filename]
	}
}
