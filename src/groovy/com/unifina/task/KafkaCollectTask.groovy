package com.unifina.task;

import grails.converters.JSON

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.kafka.KafkaFeedFileName
import com.unifina.feed.kafka.KafkaFeedFileWriter
import com.unifina.kafkaclient.UnifinaKafkaChannelConsumer
import com.unifina.kafkaclient.UnifinaKafkaConsumer
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.kafkaclient.UnifinaKafkaMessageHandler
import com.unifina.service.FeedFileService

/**
 * Collects data from Kafka for a given Stream, sends the file to
 * file storage and writes a corresponding FeedFile entry.
 */
public class KafkaCollectTask extends AbstractTask {

	private FeedFileService feedFileService;

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
		
		final long beginTime = (config.beginDate instanceof String ? df.parse(config.beginDate).time : config.beginDate)
		final long endTime = (config.endDate instanceof String ? df.parse(config.endDate).time : config.endDate)
		String beginTimeAsString = new Date(beginTime).toString()
		Map streamConfig = JSON.parse(stream.streamConfig)
		String topic = streamConfig.topic
		
		String name = config.filename
		
		final KafkaFeedFileWriter writer = new KafkaFeedFileWriter(name)
		
		int counter = 0
		
		UnifinaKafkaConsumer consumer = new UnifinaKafkaConsumer(grailsApplication.config.unifina.kafka.toProperties())
		UnifinaKafkaChannelConsumer channelConsumer = consumer.subscribe(topic, new UnifinaKafkaMessageHandler() {			
			@Override
			public void handleMessage(UnifinaKafkaMessage msg, String topic, int partition, long offset) {
				try {
					counter++
					writer.write(msg.toBytes())
				} catch (IOException e) {
					log.error("Error writing to file!",e)
				}
			}
		}, beginTime, endTime)
		
		// Wait for the Kafka consumption to finish (or timeout in case there are no messages after endDate!)
		// It may be possible to change the timeout system into something better in the future
		while (!channelConsumer.isClosed() && consumer.getTimeSinceLastEvent() < 120*1000L) {
			Thread.sleep(1000L);
			
			if (consumer.getLatestMessage()==null)
				log.info("Waiting to start seeking topic $topic...")
			else if (counter==0)
				log.info("Seeking $topic, currently at ${new Date(consumer.getLatestMessage().getTimestamp())}, beginTime is ${beginTimeAsString}")
			else
				log.info("Recorded $counter messages for $topic, now at ${new Date(consumer.getLatestMessage().getTimestamp())}")
		}
		consumer.close()
		writer.close()
		
		log.info("Done, found $counter messages for $topic.")
		
		feedFileService.createFeedFile(stream, new Date(beginTime), new Date(endTime), writer.getFile(), true)
		
		// Clean up the temp files
		writer.deleteFile()
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
	public static Map<String,Object> getConfig(Stream stream, Date beginDate, Date endDate) {
		String filename = new KafkaFeedFileName(stream, beginDate).toString()
		return [streamId:stream.id, beginDate:beginDate.time, endDate:endDate.time, filename:filename]
	}
}
