package com.unifina.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.data.StreamPartitioner
import com.unifina.data.StreamrBinaryMessage
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.feed.AbstractDataRangeProvider
import com.unifina.feed.AbstractStreamListener
import com.unifina.feed.DataRange
import com.unifina.feed.FieldDetector
import com.unifina.feed.redis.StreamrBinaryMessageWithKafkaMetadata
import com.unifina.signalpath.RuntimeRequest
import com.unifina.utils.CSVImporter
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.springframework.util.Assert

import javax.annotation.Nullable
import java.nio.charset.Charset
import java.text.DateFormat

class StreamService {

	def grailsApplication
	KafkaService kafkaService
	CassandraService cassandraService
	PermissionService permissionService

	private final StreamPartitioner partitioner = new StreamPartitioner()

	// Use Gson instead of Grails "as JSON" converter because there's no easy way to get that working in func tests that want to produce data to Streams
	private Gson gson = new GsonBuilder()
		.serializeNulls()
		.setDateFormat(DateFormat.LONG)
		.create()

	private static final Charset utf8 = Charset.forName("UTF-8")

	Stream getStream(String id) {
		return Stream.get(id)
	}

	Stream getStreamByUiChannelPath(String uiChannelPath) {
		return Stream.findByUiChannelPath(uiChannelPath)
	}

	Stream findByName(String name) {
		return Stream.findByName(name)
	}

	Stream createStream(Map params, SecUser user, String id = IdGenerator.getShort()) {
		Stream stream = new Stream(params)
		stream.id = id
		stream.user = user
		stream.config = params.config

		// If no feed given, API feed is used
		if (stream.feed == null) {
			stream.feed = Feed.load(Feed.KAFKA_ID)
		}
		Map config = stream.getStreamConfigAsMap()
		if (!config.fields) {
			config.fields = []
		}
		AbstractStreamListener streamListener = instantiateListener(stream)
		streamListener.addToConfiguration(config, stream)
		stream.config = gson.toJson(config)

		if (!stream.validate()) {
			throw new ValidationException(stream.errors)
		}

		stream.save(failOnError: true)
		if (streamListener) {
			streamListener.afterStreamSaved(stream)
		}
		return stream
	}

	void deleteStream(Stream stream) {
		AbstractStreamListener streamListener = instantiateListener(stream)
		streamListener.beforeDelete(stream)
		stream.delete(flush:true)
	}

	void deleteStreamsDelayed(List<Stream> streams, long delayMs=30*60*1000) {
		Map config = DelayedDeleteStreamTask.getConfig(streams)
		Task task = new Task(DelayedDeleteStreamTask.class.getName(), (config as JSON).toString(), "stream-delete", UUID.randomUUID().toString())
		task.runAfter = new Date(System.currentTimeMillis() + delayMs)
		task.save(flush: true, failOnError: true)
	}

	boolean autodetectFields(Stream stream, boolean flattenHierarchies) {
		FieldDetector fieldDetector = instantiateDetector(stream)
		fieldDetector.setFlattenMap(flattenHierarchies)
		def fields = fieldDetector?.detectFields(stream)
		if (fields) {
			Map config = stream.getStreamConfigAsMap()
			config.fields = fields
			stream.config = gson.toJson(config)
			stream.save(flush: true, failOnError: true)
			return true
		} else {
			return false
		}
	}

	// Ref to Kafka will be abstracted out when Feed abstraction is reworked
	@CompileStatic
	void sendMessage(Stream stream, @Nullable String partitionKey, long timestamp, byte[] content, byte contentType, int ttl=0) {
		int streamPartition = partitioner.partition(stream, partitionKey)
		StreamrBinaryMessage msg = new StreamrBinaryMessage(stream.id, streamPartition, timestamp, ttl, contentType, content)

		String kafkaPartitionKey = "${stream.id}-$streamPartition"
		kafkaService.sendMessage(msg, kafkaPartitionKey)
	}

	@CompileStatic
	void sendMessage(Stream stream, Map message, int ttl=0) {
		String str = gson.toJson(message)
		sendMessage(stream, null, System.currentTimeMillis(), str.getBytes(utf8), StreamrBinaryMessage.CONTENT_TYPE_JSON, ttl);
	}

	@CompileStatic
	void sendMessage(Stream stream, long timestamp, Map message, int ttl=0) {
		String str = gson.toJson(message)
		sendMessage(stream, null, timestamp, str.getBytes(utf8), StreamrBinaryMessage.CONTENT_TYPE_JSON, ttl);
	}

	@CompileStatic
	void sendMessage(Stream stream, @Nullable String partitionKey, Map message, int ttl=0) {
		String str = gson.toJson(message)
		sendMessage(stream, partitionKey, System.currentTimeMillis(), str.getBytes(utf8), StreamrBinaryMessage.CONTENT_TYPE_JSON, ttl);
	}

	@CompileStatic
	void sendMessage(Stream stream, long timestamp, @Nullable String partitionKey, Map message, int ttl=0) {
		String str = gson.toJson(message)
		sendMessage(stream, partitionKey, timestamp, str.getBytes(utf8), StreamrBinaryMessage.CONTENT_TYPE_JSON, ttl);
	}

	// Ref to Cassandra will be abstracted out when Feed abstraction is reworked
	@CompileStatic
	void saveMessage(Stream stream, @Nullable String partitionKey, long timestamp, byte[] content, byte contentType, int ttl, long messageNumber, Long previousMessageNumber) {
		int streamPartition = partitioner.partition(stream, partitionKey)
		// Fake Kafka partition to be 0 (does not matter)
		StreamrBinaryMessageWithKafkaMetadata msg = new StreamrBinaryMessageWithKafkaMetadata(stream.id, streamPartition, timestamp, ttl, contentType, content, 0, messageNumber, previousMessageNumber)
		cassandraService.save(msg)
	}

	@CompileStatic
	void saveMessage(Stream stream, @Nullable String partitionKey, long timestamp, Map message, int ttl, long messageNumber, Long previousMessageNumber) {
		int streamPartition = partitioner.partition(stream, partitionKey)
		String str = gson.toJson(message)
		// Fake Kafka partition to be 0 (does not matter)
		StreamrBinaryMessageWithKafkaMetadata msg = new StreamrBinaryMessageWithKafkaMetadata(stream.id, streamPartition, timestamp, ttl, StreamrBinaryMessage.CONTENT_TYPE_JSON, str.getBytes(utf8), 0, messageNumber, previousMessageNumber)
		cassandraService.save(msg)
	}

	// Ref to Cassandra will be abstracted out when Feed abstraction is reworked
	@CompileStatic
	void deleteDataRange(Stream stream, Date from, Date to) {
		cassandraService.deleteRange(stream, from, to)
	}

	// Ref to Cassandra will be abstracted out when Feed abstraction is reworked
	@CompileStatic
	void deleteDataUpTo(Stream stream, Date to) {
		cassandraService.deleteUpTo(stream, to)
	}

	// Ref to Cassandra will be abstracted out when Feed abstraction is reworked
	@CompileStatic
	void deleteAllData(Stream stream) {
		cassandraService.deleteAll(stream)
	}

	/**
	 * Imports data from a csv file into a Stream.
	 * @param csv
	 * @param stream
     * @return Autocreated Stream field config as a Map (can be written to stream.config as JSON)
     */
	@CompileStatic
	public Map importCsv(CSVImporter csv, Stream stream) {
		/**
		 * Batch-imported rows have negative Kafka offsets to avoid collisions with messages actually produced
		 * to the stream via Kafka.
		 */
		List<Long> latestOffsetByPartition = (0..stream.getPartitions()-1).collect { Integer partition ->
			StreamrBinaryMessageWithKafkaMetadata msg = cassandraService.getLatestBeforeOffset(stream, partition, 0)
			return msg ? msg.offset : Long.MIN_VALUE
		}

		for (CSVImporter.LineValues line : csv) {
			Date date = line.getTimestamp()

			// Write all fields into the message except for the timestamp column
			Map message = [:]
			for (int i=0; i<line.values.length; i++) {
				if (i!=line.schema.timestampColumnIndex && line.values[i]!=null) {
					String name = line.schema.entries[i].name
					message[name] = line.values[i]
				}
			}

			int partition = partitioner.partition(stream, null)
			long offset = latestOffsetByPartition[partition] + 1
			latestOffsetByPartition[partition] = offset
			saveMessage(stream, null, date.time, message, 0, offset, offset > Long.MIN_VALUE ? offset-1 : null)
		}

		// Autocreate the stream config based on fields in the csv schema
		Map config = (Map) (stream.config ? JSON.parse(stream.config) : [:])

		List fields = []

		// The primary timestamp column is implicit, so don't include it in streamConfig
		for (int i=0; i < csv.schema.entries.length; i++) {
			if (i != csv.getSchema().timestampColumnIndex) {
				CSVImporter.SchemaEntry e = csv.getSchema().entries[i]
				if (e!=null)
					fields << [name:e.name, type:e.type]
			}
		}

		config.fields = fields
		return config
	}

	// TODO: move to FeedService
	public AbstractStreamListener instantiateListener(Stream stream) {
		Assert.notNull(stream.feed.streamListenerClass, "feed's streamListenerClass is unexpectedly null")
		Class clazz = getClass().getClassLoader().loadClass(stream.feed.streamListenerClass)
		return clazz.newInstance(grailsApplication)
	}

	// TODO: move to FeedService
	private FieldDetector instantiateDetector(Stream stream) {
		if (stream.feed.fieldDetectorClass == null) {
			return null
		} else {
			Class clazz = getClass().getClassLoader().loadClass(stream.feed.fieldDetectorClass)
			return clazz.newInstance(grailsApplication)
		}

	}

	// TODO: move to FeedService
	private AbstractDataRangeProvider instantiateDataRangeProvider(Stream stream) {
		if (stream.feed.dataRangeProviderClass == null) {
			return null
		} else {
			Class clazz = getClass().getClassLoader().loadClass(stream.feed.dataRangeProviderClass)
			return clazz.newInstance(grailsApplication)
		}
	}

	DataRange getDataRange(Stream stream) {
		AbstractDataRangeProvider provider = instantiateDataRangeProvider(stream)
		if (provider)
			return provider.getDataRange(stream)
		else return null
	}

	@CompileStatic
	void getReadAuthorizedStream(String id, SecUser user, Key key, Closure action) {
		def stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream", id)
		}

		if (key != null) {
			if (isDirectPermissionToStream(key, stream)) {
				action.call(stream)
			} else {
				throw new NotPermittedException(null, "Stream", id, Permission.Operation.READ.id)
			}
		} else {
			if (isDirectPermissionToStream(user, stream) || isPermissionToStreamsUiChannelCanvas(user, stream) || isPermissionToStreamViaDashboard(user, stream)) {
				action.call(stream)
			} else {
				throw new NotPermittedException(user?.username, "Stream", id, Permission.Operation.READ.id)
			}
		}
	}

	@CompileStatic
	private boolean isDirectPermissionToStream(SecUser user, Stream stream) {
		return permissionService.canRead(user, stream)
	}


	@CompileStatic
	private boolean isDirectPermissionToStream(Key key, Stream stream) {
		return permissionService.canRead(key, stream)
	}

	@CompileStatic
	private boolean isPermissionToStreamsUiChannelCanvas(SecUser user, Stream stream) {
		return stream.uiChannel && stream.uiChannelCanvas && permissionService.canRead(user, stream.uiChannelCanvas)
	}

	private boolean isPermissionToStreamViaDashboard(SecUser user, Stream stream) {
		def dashboardService = grailsApplication.mainContext.getBean(DashboardService) // Don't initialize normally, preventing circular service dependency loop
		if (stream.uiChannel && stream.uiChannelCanvas != null && stream.uiChannelPath != null) {
			Canvas canvas = stream.uiChannelCanvas
			int moduleId = parseModuleId(stream.uiChannelPath)
			List<DashboardItem> matchedItems = DashboardItem.findAllByCanvasAndModule(canvas, moduleId)
			for (DashboardItem item : matchedItems) {
				if (dashboardService.authorizedGetDashboardItem(item.dashboard.id, item.id, user, Permission.Operation.READ)) {
					return true
				}
			}
		}
		return false
	}

	@CompileStatic
	private static Integer parseModuleId(String path) {
		RuntimeRequest.PathReader reader = new RuntimeRequest.PathReader(path.substring(1))
		reader.readCanvasId()
		return reader.readModuleId()
	}
}
