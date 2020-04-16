package com.unifina.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.streamr.client.protocol.message_layer.StreamMessage
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.data.StreamPartitioner
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.data.Stream
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.feed.DataRange
import com.unifina.feed.FieldDetector

import com.unifina.signalpath.RuntimeRequest
import com.unifina.task.DelayedDeleteStreamTask
import com.unifina.utils.EthereumAddressValidator
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

import java.text.DateFormat

class StreamService {

	GrailsApplication grailsApplication

	CassandraService cassandraService
	PermissionService permissionService

	private final StreamPartitioner partitioner = new StreamPartitioner()

	// Use Gson instead of Grails "as JSON" converter because there's no easy way to get that working in func tests that want to produce data to Streams
	private Gson gson = new GsonBuilder()
		.serializeNulls()
		.setDateFormat(DateFormat.LONG)
		.create()

	Stream getStream(String id) {
		if (EthereumAddressValidator.validate(id)) {
			Stream inboxStream = Stream.get(id.toLowerCase())
			if (inboxStream.inbox) {
				return inboxStream
			}
		}
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

		Map config = stream.getStreamConfigAsMap()
		if (!config.fields) {
			config.fields = []
		}
		stream.config = gson.toJson(config)

		if (!stream.validate()) {
			throw new ValidationException(stream.errors)
		}

		stream.save(failOnError: true)
		permissionService.systemGrantAll(user, stream)

		return stream
	}

	void deleteStream(Stream stream) {
		cassandraService.deleteAll(stream)
		stream.delete(flush:true)
	}

	void deleteStreamsDelayed(List<Stream> streams, long delayMs=30*60*1000) {
		Map config = DelayedDeleteStreamTask.getConfig(streams)
		Task task = new Task(DelayedDeleteStreamTask.class.getName(), (config as JSON).toString(), "stream-delete", UUID.randomUUID().toString())
		task.runAfter = new Date(System.currentTimeMillis() + delayMs)
		task.save(flush: true, failOnError: true)
	}

	boolean autodetectFields(Stream stream, boolean flattenHierarchies, boolean saveFields) {
		StreamMessage latest = cassandraService.getLatestFromAllPartitions(stream)

		if (!latest) {
			return false
		} else {
			List<FieldDetector.FieldConfig> fields = FieldDetector.detectFields(latest, flattenHierarchies)
			Map config = stream.getStreamConfigAsMap()
			config.fields = fields*.toMap()
			stream.config = gson.toJson(config)
			if (saveFields) {
				stream.save(flush: true, failOnError: true)
			} else {
				stream.discard()
			}
			return true
		}
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

	@CompileStatic
	DataRange getDataRange(Stream stream) {
		return cassandraService.getDataRange(stream)
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
				throw new NotPermittedException(user?.email, "Stream", id, Permission.Operation.READ.id)
			}
		}
	}

	Set<String> getStreamEthereumPublishers(Stream stream) {
		// This approach might be slow if there are a lot of allowed writers to the Stream
		List<SecUser> writers = permissionService.getPermissionsTo(stream, Permission.Operation.WRITE)*.user

		List<IntegrationKey> keys = IntegrationKey.createCriteria().list {
			user {
				'in'("id", writers*.id)
			}
			'in'("service", [IntegrationKey.Service.ETHEREUM, IntegrationKey.Service.ETHEREUM_ID])
		}

		return keys*.idInService*.toLowerCase() as Set
	}

	boolean isStreamEthereumPublisher(Stream stream, String ethereumAddress) {
		IntegrationKey key = IntegrationKey.createCriteria().get {
			ilike("idInService", ethereumAddress)
		}
		if (key == null || key.user == null) {
			return false
		}
		return permissionService.canWrite(key.user, stream)
	}

	Set<String> getStreamEthereumSubscribers(Stream stream) {
		// This approach might be slow if there are a lot of allowed readers to the Stream
		List<SecUser> readers = permissionService.getPermissionsTo(stream, Permission.Operation.READ)*.user

		List<IntegrationKey> keys = IntegrationKey.createCriteria().list {
			user {
				'in'("id", readers*.id)
			}
			'in'("service", [IntegrationKey.Service.ETHEREUM, IntegrationKey.Service.ETHEREUM_ID])
		}

		return keys*.idInService*.toLowerCase() as Set
	}

	boolean isStreamEthereumSubscriber(Stream stream, String ethereumAddress) {
		IntegrationKey key = IntegrationKey.createCriteria().get {
			ilike("idInService", ethereumAddress)
		}
		if (key == null || key.user == null) {
			return false
		}
		return permissionService.canRead(key.user, stream)
	}

	List<Stream> getInboxStreams(List<SecUser> users) {
		if (users.isEmpty()) return new ArrayList<Stream>()
		List<IntegrationKey> keys = IntegrationKey.createCriteria().list {
			user {
				'in'("id", users*.id)
			}
			'in'("service", [IntegrationKey.Service.ETHEREUM, IntegrationKey.Service.ETHEREUM_ID])
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
