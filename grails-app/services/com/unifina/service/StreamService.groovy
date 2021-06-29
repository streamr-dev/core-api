package com.unifina.service

import com.unifina.domain.*
import com.unifina.utils.JSONUtil
import grails.validation.Validateable
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.exception.ConstraintViolationException
import org.hibernate.exception.DataException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.validation.FieldError

@Validateable
@ToString
@EqualsAndHashCode
class CreateStreamCommand {
	String id
	String name
	String description
	Map<String, Object> config
	Integer partitions = 1
	Boolean requireSignedData = false
	Boolean requireEncryptedData = false
	Boolean autoConfigure = true
	Integer storageDays = Stream.DEFAULT_STORAGE_DAYS
	Integer inactivityThresholdHours = Stream.DEFAULT_INACTIVITY_THRESHOLD_HOURS
}

class StreamService {
	PermissionService permissionService
	private final StreamPartitioner partitioner = new StreamPartitioner()

	Stream getStream(String id) {
		return Stream.get(id)
	}

	Stream createStream(CreateStreamCommand cmd, User user, CustomStreamIDValidator customStreamIDValidator) {
		Stream stream = new Stream(
			description: cmd.description,
			config: JSONUtil.createGsonBuilder().toJson(Stream.normalizeConfig(cmd.config)),
			partitions: cmd.partitions,
			requireSignedData: cmd.requireSignedData,
			requireEncryptedData: cmd.requireEncryptedData,
			autoConfigure: cmd.autoConfigure,
			storageDays: cmd.storageDays,
			inactivityThresholdHours: cmd.inactivityThresholdHours,
		)
		stream.id = cmd.id
		if ((customStreamIDValidator != null) && (!customStreamIDValidator.validate(cmd.id, user))) {
			throw new ValidationException(new FieldError("stream", "id", cmd.id, false, new String[0], new String[0], null))
		}
		if (cmd.name == null || cmd.name.trim() == "") {
			stream.name = stream.id
		} else {
			stream.name = cmd.name
		}

		if (!stream.validate()) {
			throw new ValidationException(stream.errors)
		}

		try {
			stream.save(flush: true, failOnError: true)
		} catch (final ConstraintViolationException | DataException e) {
			final Throwable cause = e.getCause()
			if (cause == null) {
				log.error(e)
				return
			}
			final String message = cause.getMessage()
			final String errorPrefix = "Data truncation: Data too long for column"
			if (message.startsWith(errorPrefix)) {
				final List<String> names = new ArrayList<>()
				names.add("id")
				names.add("description")
				names.add("name")
				boolean isLogged = true
				for (String name : names) {
					final boolean doNotLogThisError = message.contains("'" + name + "'")
					if (doNotLogThisError) {
						isLogged = false
						break
					}
				}
				if (isLogged) {
					log.error(e)
				}
			}
		} catch (DataIntegrityViolationException e) {
			// the failed integrity is most likely stream.id (the only reference field that can be set manually by the user)
			throw new DuplicateNotAllowedException("Stream", stream.id)
		}
		permissionService.systemGrantAll(user, stream)
		return stream
	}

	void deleteStream(Stream stream) {
		stream.delete(flush: true)
	}

	Set<String> getStreamEthereumPublishers(Stream stream) {
		// This approach might be slow if there are a lot of allowed writers to the Stream
		List<User> writers = permissionService.getPermissionsTo(stream, Permission.Operation.STREAM_PUBLISH)*.user

		List<User> users = User.createCriteria().list {
			'in'("id", writers*.id)
		}

		Set<String> publishers = new HashSet<>()
		publishers.addAll(users*.getUsername()*.toLowerCase())
		return publishers
	}

	boolean isStreamEthereumPublisher(Stream stream, String ethereumAddress) {
		User user = User.createCriteria().get {
			// ilike = case-insensitive like: Ethereum addresses are case-insensitive but
			// different case systems are in use (checksum-case, lower-case at least)
			ilike("username", ethereumAddress)
		}
		if (user == null) {
			return false
		}
		return permissionService.check(user, stream, Permission.Operation.STREAM_PUBLISH)
	}

	Set<String> getStreamEthereumSubscribers(Stream stream) {
		// This approach might be slow if there are a lot of allowed readers to the Stream
		List<User> readers = permissionService.getPermissionsTo(stream, Permission.Operation.STREAM_SUBSCRIBE)*.user

		List<User> users = User.createCriteria().list {
			'in'("id", readers*.id)
		}

		return users*.username*.toLowerCase() as Set
	}

	boolean isStreamEthereumSubscriber(Stream stream, String ethereumAddress) {
		User user = User.createCriteria().get {
			// ilike = case-insensitive like: Ethereum addresses are case-insensitive but
			// different case systems are in use (checksum-case, lower-case at least)
			ilike("username", ethereumAddress)
		}
		if (user == null) {
			return false
		}
		return permissionService.check(user, stream, Permission.Operation.STREAM_SUBSCRIBE)
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
