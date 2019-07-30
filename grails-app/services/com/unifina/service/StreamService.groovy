package com.unifina.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.protocol.message_layer.StreamMessageV31
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.data.StreamPartitioner
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.feed.AbstractStreamListener
import com.unifina.feed.DataRange
import com.unifina.feed.DataRangeProvider
import com.unifina.feed.FieldDetector
import com.unifina.signalpath.RuntimeRequest
import com.unifina.task.DelayedDeleteStreamTask
import com.unifina.utils.CSVImporter
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.springframework.util.Assert

import java.text.DateFormat

class StreamService {

	def grailsApplication
	FeedService feedService
	KafkaService kafkaService
	CassandraService cassandraService
	PermissionService permissionService

	private final StreamPartitioner partitioner = new StreamPartitioner()

	// Use Gson instead of Grails "as JSON" converter because there's no easy way to get that working in func tests that want to produce data to Streams
	private Gson gson = new GsonBuilder()
		.serializeNulls()
		.setDateFormat(DateFormat.LONG)
		.create()

	Stream getStream(String id) {
		return Stream.get(id)
	}

	Stream getStreamByUiChannelPath(String uiChannelPath) {
		return Stream.findByUiChannelPath(uiChannelPath)
	}

	Stream createStream(Map params, SecUser user, String id = IdGenerator.getShort()) {
		Stream stream = new Stream(params)
		stream.id = id
		stream.config = params.config
		if (stream.name == null || stream.name.trim() == "") {
			stream.name = Stream.DEFAULT_NAME
		}

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
		permissionService.systemGrantAll(user, stream)

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

	boolean autodetectFields(Stream stream, boolean flattenHierarchies, boolean saveFields) {
		FieldDetector fieldDetector = instantiateDetector(stream)
		fieldDetector.setFlattenMap(flattenHierarchies)
		def fields = fieldDetector?.detectFields(stream)
		if (fields) {
			Map config = stream.getStreamConfigAsMap()
			config.fields = fields
			stream.config = gson.toJson(config)
			if (saveFields) {
				stream.save(flush: true, failOnError: true)
			} else {
				stream.discard()
			}
			return true
		} else {
			return false
		}
	}

	// Ref to Kafka will be abstracted out when Feed abstraction is reworked

	void sendMessage(StreamMessage msg) {
		String kafkaPartitionKey = "${msg.getStreamId()}-${msg.getStreamPartition()}"
		kafkaService.sendMessage(msg, kafkaPartitionKey)
	}

	void saveMessage(StreamMessage msg) {
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
	public Map importCsv(CSVImporter csv, Stream stream, String publisherId) {
		long sequenceNumber = 0L
		Long previousTimestamp = null
		String msgChainId = IdGenerator.getShort()

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
			StreamMessageV31 msg = new StreamMessageV31(stream.id, partition, date.time, sequenceNumber, publisherId, msgChainId,
				previousTimestamp, sequenceNumber, StreamMessage.ContentType.CONTENT_TYPE_JSON, StreamMessage.EncryptionType.NONE,
				gson.toJson(message), StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
			saveMessage(msg)
			sequenceNumber++
			previousTimestamp = date.time
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
		return clazz.newInstance()
	}

	// TODO: move to FeedService
	private FieldDetector instantiateDetector(Stream stream) {
		if (stream.feed.fieldDetectorClass == null) {
			return null
		} else {
			Class clazz = getClass().getClassLoader().loadClass(stream.feed.fieldDetectorClass)
			return clazz.newInstance()
		}
	}

	@CompileStatic
	DataRange getDataRange(Stream stream) {
		DataRangeProvider provider = feedService.instantiateDataRangeProvider(stream.feed)
		return provider?.getDataRange(stream)
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

	Set<String> getStreamEthereumPublishers(Stream stream) {
		// This approach might be slow if there are a lot of allowed writers to the Stream
		List<SecUser> writers = permissionService.getPermissionsTo(stream, Permission.Operation.WRITE)*.user

		List<IntegrationKey> keys = IntegrationKey.findAll {
			user.id in writers*.id && service in [IntegrationKey.Service.ETHEREUM, IntegrationKey.Service.ETHEREUM_ID]
		}

		return keys*.idInService as Set
	}

	boolean isStreamEthereumPublisher(Stream stream, String ethereumAddress) {
		IntegrationKey key = IntegrationKey.findByIdInService(ethereumAddress)
		if (key == null || key.user == null) {
			return false
		}
		return permissionService.canWrite(key.user, stream)
	}

	Set<String> getStreamEthereumSubscribers(Stream stream) {
		// This approach might be slow if there are a lot of allowed readers to the Stream
		List<SecUser> readers = permissionService.getPermissionsTo(stream, Permission.Operation.READ)*.user

		List<IntegrationKey> keys = IntegrationKey.findAll {
			user.id in readers*.id && service in [IntegrationKey.Service.ETHEREUM, IntegrationKey.Service.ETHEREUM_ID]
		}

		return keys*.idInService as Set
	}

	boolean isStreamEthereumSubscriber(Stream stream, String ethereumAddress) {
		IntegrationKey key = IntegrationKey.findByIdInService(ethereumAddress)
		if (key == null || key.user == null) {
			return false
		}
		return permissionService.canRead(key.user, stream)
	}

	List<Stream> getInboxStreams(List<SecUser> users) {
		if (users.isEmpty()) return new ArrayList<Stream>()
		List<IntegrationKey> keys = IntegrationKey.findAll {
			user.id in users*.id && service in [IntegrationKey.Service.ETHEREUM, IntegrationKey.Service.ETHEREUM_ID]
		}
		return Stream.findAllByIdInListAndInbox(keys*.idInService, true)
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

	static class StreamStatus {
		Boolean ok
		Date date
		StreamStatus(Boolean ok, Date date) {
			this.ok = ok
			this.date = date
		}
	}

	@CompileStatic
	StreamStatus status(Stream s, Date now) {
		StreamMessage msg = cassandraService.getLatestFromAllPartitions(s)
		if (s.inactivityThresholdHours == 0) {
			if (msg == null) {
				return new StreamStatus(true, null)
			} else {
				return new StreamStatus(true, msg.getTimestampAsDate())
			}
		}
		if (msg == null) {
			return new StreamStatus(false, null)
		} else if (msg != null && s.isStale(now, msg.getTimestampAsDate())) {
			return new StreamStatus(false, msg.getTimestampAsDate())
		}
		return new StreamStatus(true, msg.getTimestampAsDate())
	}

	@CompileStatic
	def addExampleStreams(SecUser user, List<Stream> examples) {
		for (final Stream example : examples) {
			// Grant read permission to example stream.
			permissionService.systemGrant(user, example, Permission.Operation.READ)
		}
	}
}
