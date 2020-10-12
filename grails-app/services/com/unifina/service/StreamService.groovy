package com.unifina.service


import com.streamr.client.protocol.message_layer.StreamMessage
import com.unifina.data.StreamPartitioner
import com.unifina.domain.*
import com.unifina.feed.DataRange
import com.unifina.feed.FieldDetector
import com.unifina.task.DelayedDeleteStreamTask
import com.unifina.utils.IdGenerator
import com.unifina.utils.JSONUtil
import org.springframework.dao.DataIntegrityViolationException
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.validation.FieldError

@Validateable
@ToString
@EqualsAndHashCode
class CreateStreamCommand {
	String id
	String name
	String description
	Map<String,Object> config
	Integer partitions = 1
	Boolean uiChannel = false
	Boolean requireSignedData = false
	Boolean requireEncryptedData = false
}

class StreamService {

	GrailsApplication grailsApplication

	CassandraService cassandraService
	PermissionService permissionService

	private final StreamPartitioner partitioner = new StreamPartitioner()

	Stream getStream(String id) {
		return Stream.get(id)
	}

	Stream getStreamByUiChannelPath(String uiChannelPath) {
		return Stream.findByUiChannelPath(uiChannelPath)
	}

	Stream createStream(CreateStreamCommand cmd, User user) {
		return createStream(cmd, user, null, null, true)
	}

	Stream createStream(CreateStreamCommand cmd, User user, String uiChannelPath, Canvas uiChannelCanvas, boolean validateIDField) {
		Stream stream = new Stream(
			name: ((cmd.name == null || cmd.name.trim() == "")) ? Stream.DEFAULT_NAME : cmd.name,
			description: cmd.description,
			config: JSONUtil.createGsonBuilder().toJson(Stream.normalizeConfig(cmd.config)),
			partitions: cmd.partitions,
			uiChannel: cmd.uiChannel,
			requireSignedData: cmd.requireSignedData,
			requireEncryptedData: cmd.requireEncryptedData,
			uiChannelPath: uiChannelPath,
			uiChannelCanvas: uiChannelCanvas
		)
		if (cmd.id != null) {
			if (validateIDField && !CustomStreamIDValidator.validate(cmd.id)) {
				throw new ValidationException(new FieldError("stream", "id", null))
			}
			stream.id = cmd.id
		} else {
			stream.id = IdGenerator.getShort()
		}

		if (!stream.validate()) {
			throw new ValidationException(stream.errors)
		}

		try {
			stream.save(flush:true, failOnError: true)
		} catch (DataIntegrityViolationException e) {
			// the failed integrity is most likely stream.id (the only reference field that can be set manually by the user)
			throw new DuplicateNotAllowedException("Stream", stream.id)
		}
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
		task.save(flush: false, failOnError: true)
	}

	boolean autodetectFields(Stream stream, boolean flattenHierarchies, boolean saveFields) {
		StreamMessage latest = cassandraService.getLatestFromAllPartitions(stream)

		if (!latest) {
			return false
		} else {
			List<FieldDetector.FieldConfig> fields = FieldDetector.detectFields(latest, flattenHierarchies)
			Map config = Stream.getStreamConfigAsMap(stream.config)
			config.fields = fields*.toMap()
			stream.config = JSONUtil.createGsonBuilder().toJson(config)
			if (saveFields) {
				stream.save(flush: false, failOnError: true)
			} else {
				stream.discard()
			}
			return true
		}
	}

	void saveMessage(StreamMessage msg) {
		cassandraService.save(msg)
	}

	@CompileStatic
	DataRange getDataRange(Stream stream) {
		return cassandraService.getDataRange(stream)
	}

	Set<String> getStreamEthereumPublishers(Stream stream) {
		// This approach might be slow if there are a lot of allowed writers to the Stream
		List<User> writers = permissionService.getPermissionsTo(stream, Permission.Operation.STREAM_PUBLISH)*.user

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
			ilike("idInService", ethereumAddress) // ilike = case-insensitive like: Ethereum addresses are case-insensitive but different case systems are in use (checksum-case, lower-case at least)
		}
		if (key == null || key.user == null) {
			return false
		}
		return permissionService.check(key.user, stream, Permission.Operation.STREAM_PUBLISH)
	}

	Set<String> getStreamEthereumSubscribers(Stream stream) {
		// This approach might be slow if there are a lot of allowed readers to the Stream
		List<User> readers = permissionService.getPermissionsTo(stream, Permission.Operation.STREAM_SUBSCRIBE)*.user

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
		return permissionService.check(key.user, stream, Permission.Operation.STREAM_SUBSCRIBE)
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
	def addExampleStreams(User user, List<Stream> examples) {
		for (final Stream example : examples) {
			// Grant read permission to example stream.
			permissionService.systemGrant(user, example, Permission.Operation.STREAM_GET)
			permissionService.systemGrant(user, example, Permission.Operation.STREAM_SUBSCRIBE)
		}
	}
}
