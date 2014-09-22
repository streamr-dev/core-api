package com.unifina.task;

import grails.converters.JSON

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.GZIPOutputStream

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.kafka.KafkaFeedFileName
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
		Map streamConfig = JSON.parse(stream.streamConfig)
		String topic = streamConfig.topic
		
		String name = config.filename
		
		File dir = File.createTempDir(name, "")
		File file = new File(dir, name)
		GZIPOutputStream gzos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file)))
		
		final AtomicBoolean done = new AtomicBoolean(false);
		
		UnifinaKafkaConsumer consumer = new UnifinaKafkaConsumer(grailsApplication.config.unifina.kafka.toProperties())
		consumer.subscribe(topic, new UnifinaKafkaMessageHandler() {
			ByteBuffer lengthBuffer;
			
			@Override
			public void handleMessage(UnifinaKafkaMessage msg) {
				if (done.get())
					return
				else if (msg.getTimestamp()>=endTime) {
					done.set(true)
					return
				}
				else {
					try {
						byte[] rawMsg = msg.toBytes()
						lengthBuffer = ByteBuffer.allocate(4);
						lengthBuffer.order(ByteOrder.BIG_ENDIAN);
						byte[] length = lengthBuffer.putInt(rawMsg.length).array();
						gzos.write(length)
						gzos.write(rawMsg)
					} catch (IOException e) {
						log.error("Error writing to file!",e)
					}
				}
			}
		}, beginTime)
		
		// Wait for the Kafka consumption to finish (or timeout in case there are no messages after endDate!)
		// It may be possible to change the timeout system into something better in the future
		while (!done.get() && consumer.getTimeSinceLastEvent() < 60000L) {
			Thread.sleep(1000L);
		}
		consumer.close()
		gzos.finish()
		gzos.close()
		
		// Check that the FeedFile does not exist
		FeedFile feedFile = feedFileService.getFeedFile(stream.feed, new Date(beginTime), new Date(endTime), name)
		
		// If the FeedFile entry already exists, delete it
		if (feedFile) {
			log.info("Unprocessed FeedFile already exists. Deleting it and creating a new entry...")
			feedFile.delete()
		}
		
		// Send file to file storage service if its size is greater than zero
		if (file.length()>0) {
			FeedFile.withTransaction {
				feedFile = new FeedFile()
				feedFile.name = name
				feedFile.beginDate = new Date(beginTime)
				feedFile.endDate = new Date(endTime)
				// TODO: remove deprecated
				feedFile.day = feedFile.beginDate
				
				feedFile.processed = false
				feedFile.processing = true
				feedFile.processTaskCreated = false
				feedFile.feed = stream.feed
				feedFile.stream = stream
				feedFile.save(flush:true, failOnError:true)
			}
			
			feedFileService.storeFile(file, feedFile)
			
			FeedFile.withTransaction {
				feedFile.attach()
				feedFile.processed = true
				feedFile.processing = false
				feedFile.save(flush:true, failOnError:true)
			}
			
			Stream.withTransaction {
				// Use pessimistic locking for updating the Stream
				stream = Stream.lock(stream.id)
				if (stream.firstHistoricalDay==null || stream.firstHistoricalDay.time > beginTime)
					stream.firstHistoricalDay = new Date(beginTime)
				if (stream.lastHistoricalDay==null || stream.lastHistoricalDay.time < endTime)
					stream.lastHistoricalDay = new Date(endTime)
					
				stream.save(flush:true, failOnError:true)
			}
		}
		
		// Clean up the temp files
		file.delete()
		dir.delete()
		
	}

	@Override
	public void onComplete(boolean taskGroupComplete) {
		
	}
	
	public static Map<String,Object> getConfig(Stream stream, Date beginDate, Date endDate) {
		String filename = new KafkaFeedFileName(stream, beginDate).toString()
		return [streamId:stream.id, beginDate:beginDate.time, endDate:endDate.time, filename:filename]
	}
}
