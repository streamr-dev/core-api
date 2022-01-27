package com.unifina.controller

import com.unifina.domain.Permission
import com.unifina.domain.Stream
import com.unifina.domain.User
import com.unifina.service.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(StreamApiController)
@Mock([User, Stream, Permission, PermissionService, StreamService, RESTAPIFilters])
class StreamApiControllerSpec extends ControllerSpecification {
	StreamService streamService
	ApiService apiService

	User me
	Stream streamOne
	def streamTwoId
	def streamThreeId
	def streamFourId

	def setup() {
		controller.permissionService = mainContext.getBean(PermissionService)
		apiService = controller.apiService = Mock(ApiService)

		me = new User(username: "0x82D871cFA31cA2FD52bF98Dcce9f44FfDf47d50A")
		me.save(validate: false)

		def otherUser = new User(username: "0x9f856b2BF24d0C74969DbC54F7AF429Bf012b2F0").save(validate: false)

		// First use real streamService to create the streams
		streamService = mainContext.getBean(StreamService)
		streamService.permissionService = controller.permissionService
		streamOne = streamService.createStream(new CreateStreamCommand(id: me.username + "/stream", name: "stream", description: "description"), me, null)
		streamTwoId = streamService.createStream(new CreateStreamCommand(id: me.username + "/ztream", name: "ztream"), me, null).id
		streamThreeId = streamService.createStream(new CreateStreamCommand(id: me.username + "/atream", name: "atream"), me, null).id
		streamFourId = streamService.createStream(new CreateStreamCommand(id: me.username + "/otherUserStream", name: "otherUserStream"), otherUser, null).id

		controller.streamService = streamService
	}

	void "find all streams of logged in user"() {
		when:
		authenticatedAs(me) { controller.index() }

		then:
		response.json.length() == 3
		1 * apiService.list(Stream, {
			assert it.toMap() == new StreamListParams().toMap()
			return true
		}, me) >> [
			streamOne,
			Stream.findById(streamTwoId),
			Stream.findById(streamThreeId)
		]
	}

	void "find all streams of logged in user without config"() {
		when:
		request.setParameter("noConfig", "true")
		authenticatedAs(me) { controller.index() }

		then:
		response.json.length() == 3
		response.json[0].config == null
		1 * apiService.list(Stream, {
			assert it.toMap() == new StreamListParams().toMap()
			return true
		}, me) >> [
			streamOne,
			Stream.findById(streamTwoId),
			Stream.findById(streamThreeId)
		]
	}

	void "find streams by name of logged in user"() {
		when:
		params.name = "stream"
		authenticatedAs(me) { controller.index() }

		then:
		response.json.length() == 1
		response.json[0].id.length() == 49
		response.json[0].name == "stream"
		response.json[0].config == [
			fields: []
		]
		response.json[0].description == "description"
		1 * apiService.list(Stream, {
			assert it.toMap() == new StreamListParams(name: "stream").toMap()
			return true
		}, me) >> [
			streamOne
		]
	}

	void "index() adds name param to filter criteria"() {
		when:
		def name = Stream.get(streamTwoId).name
		params.name = name
		authenticatedAs(me) { controller.index() }

		then:
		response.json[0].name == name
		1 * apiService.list(Stream, {
			assert it.toMap() == new StreamListParams(name: "ztream").toMap()
			return true
		}, me) >> [
			Stream.findById(streamTwoId)
		]
	}

	void "creating stream fails given invalid token"() {
		when:
		request.json = [name: "Test stream", description: "Test stream"]
		request.method = 'POST'
		unauthenticated() { controller.save() }

		then:
		response.status == 401
	}

	void "save() calls StreamService.createStream() and returns it.toMap()"() {
		setup:
		controller.streamService = streamService = Mock(StreamService)
		def stream = new Stream()
		stream.id = "test-stream"


		when:
		authenticatedAs(me) { controller.save() }

		then:
		1 * streamService.createStream(_, me, _) >> { stream }
		response.json.id == stream.id
	}

	void "show a Stream of logged in user"() {
		when:
		params.id = streamOne.id
		authenticatedAs(me) { controller.show() }

		then:
		response.status == 200
		response.json.name == "stream"
	}

	void "cannot shown non-existent Stream"() {
		when:
		params.id = "666-666-666"
		authenticatedAs(me) { controller.show() }

		then:
		thrown NotFoundException
	}

	void "cannot show other user's Stream"() {
		when:
		params.id = streamFourId
		authenticatedAs(me) { controller.show() }

		then:
		NotPermittedException ex = thrown(NotPermittedException)
		ex.getUser() == me.getUsername()
	}

	void "does not show Stream if key not permitted"() {
		when:
		params.id = streamOne.id
		unauthenticated() { controller.show() }

		then:
		response.status == 401
	}

	void "update validates fields"() {
		setup:
		request.method = "PUT"
		params.id = streamOne.id
		request.JSON = [
			name: "name",
			partitions: -4,
		]

		when:
		authenticatedAs(me) { controller.update() }

		then:
		thrown ValidationException
	}

	void "update a Stream of logged in user"() {
		setup:
		request.method = "PUT"
		params.id = streamOne.id
		request.JSON = [
			name: "newName",
			description: "newDescription",
			autoConfigure: false,
			requireSignedData: true,
			storageDays: 24,
			inactivityThresholdHours: 99,
			partitions: 5,
			requireEncryptedData: true,
			migrateToBrubeck: true,
		]

		when:
		authenticatedAs(me) { controller.update() }

		then:
		response.status == 200
		response.json.name == "newName"
		response.json.description == "newDescription"
		response.json.storageDays == 24
		response.json.inactivityThresholdHours == 99
		response.json.partitions == 5
		response.json.migrateToBrubeck == true
		response.json.migrateSyncTurnedOnAt != null
		response.json.migrateSyncLastRunAt == null

		then:
		def stream = streamOne
		stream.name == "newName"
		stream.description == "newDescription"
		stream.config == null
		stream.autoConfigure == false
		stream.requireSignedData == true
		stream.requireEncryptedData == true
		stream.storageDays == 24
		stream.inactivityThresholdHours == 99
		stream.partitions == 5
		stream.migrateToBrubeck == true
		stream.migrateSyncTurnedOnAt != null
		stream.migrateSyncLastRunAt == null
	}

	void "update a Stream of logged in user but do not update undefined fields"() {
		setup:
		request.method = "PUT"
		params.id = streamOne.id
		request.json = [
			name: "newName",
			description: "newDescription",
			autoConfigure: null,
			requireSignedData: null,
			storageDays: null,
			inactivityThresholdHours: null,
			requireEncryptedData: null
		]

		when:
		authenticatedAs(me) { controller.update() }

		then:
		response.status == 200
		response.json.name == "newName"
		response.json.description == "newDescription"
		response.json.storageDays == Stream.DEFAULT_STORAGE_DAYS
		response.json.inactivityThresholdHours == Stream.DEFAULT_INACTIVITY_THRESHOLD_HOURS
		response.json.partitions == 1

		then:
		def stream = streamOne
		stream.name == "newName"
		stream.description == "newDescription"
		stream.config == null
		stream.autoConfigure == true
		stream.requireSignedData == false
		stream.requireEncryptedData == false
		stream.storageDays == Stream.DEFAULT_STORAGE_DAYS
		stream.inactivityThresholdHours == Stream.DEFAULT_INACTIVITY_THRESHOLD_HOURS
	}

	void "cannot update non-existent Stream"() {
		setup:
		request.method = "PUT"
		params.id = "666-666-666"
		request.json = [
			name: "some new name",
		]
		when:
		authenticatedAs(me) { controller.update() }

		then:
		thrown NotFoundException
	}

	void "cannot update other user's Stream"() {
		setup:
		request.method = "PUT"
		params.id = streamFourId
		request.json = [
			name: "newName",
			description: "newDescription"
		]
		when:
		authenticatedAs(me) { controller.update() }

		then:
		thrown NotPermittedException
	}

	void "delete a Stream of logged in user"() {
		when:
		params.id = streamOne.id
		request.method = "DELETE"
		authenticatedAs(me) { controller.delete() }

		then:
		response.status == 204
	}

	void "cannot delete non-existent Stream"() {
		when:
		params.id = "666-666-666"
		request.method = "DELETE"
		authenticatedAs(me) { controller.delete() }

		then:
		thrown NotFoundException
	}

	void "cannot delete other user's Stream"() {
		when:
		params.id = streamFourId
		request.method = "DELETE"
		authenticatedAs(me) { controller.delete() }

		then:
		thrown NotPermittedException
	}

	void "can set fields"() {
		when:
		params.id = streamOne.id
		request.method = "POST"
		request.JSON = ["field1": "string"]
		authenticatedAs(me) { controller.setFields() }

		then:
		1 * apiService.authorizedGetById(Stream, streamOne.id, me, Permission.Operation.STREAM_EDIT) >> streamOne
		streamOne.config == '{"fields":{"field1":"string"}}'
		response.status == 200
	}

	void "returns set of publisher addresses"() {
		setup:
		controller.streamService = streamService = Mock(StreamService)
		Set<String> addresses = new HashSet<String>()
		addresses.add('0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6')
		addresses.add('0x0181ae2f5efe8947eca8c2e9d3f32702cf4be7dd')
		when:
		params.id = streamOne.id
		request.method = "GET"
		authenticatedAs(me) { controller.publishers() }

		then:
		1 * streamService.getStreamEthereumPublishers(streamOne) >> addresses
		response.status == 200
		response.json == [
			'addresses': addresses.toArray()
		]
	}

	void "return 200 if valid publisher"() {
		setup:
		controller.streamService = streamService = Mock(StreamService)
		String address = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		when:
		params.id = streamOne.id
		params.address = address
		request.method = "GET"
		authenticatedAs(me) { controller.publisher() }

		then:
		1 * streamService.isStreamEthereumPublisher(streamOne, address) >> true
		response.status == 200
	}

	void "return 404 if invalid publisher"() {
		setup:
		controller.streamService = streamService = Mock(StreamService)
		String address = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		when:
		params.id = streamOne.id
		params.address = address
		request.method = "GET"
		authenticatedAs(me) { controller.publisher() }

		then:
		1 * streamService.isStreamEthereumPublisher(streamOne, address) >> false
		response.status == 404
	}

	void "returns set of subscriber addresses"() {
		setup:
		controller.streamService = streamService = Mock(StreamService)
		Set<String> addresses = new HashSet<String>()
		addresses.add('0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6')
		addresses.add('0x0181ae2f5efe8947eca8c2e9d3f32702cf4be7dd')
		when:
		params.id = streamOne.id
		request.method = "GET"
		authenticatedAs(me) { controller.subscribers() }

		then:
		1 * streamService.getStreamEthereumSubscribers(streamOne) >> addresses
		response.status == 200
		response.json == [
			'addresses': addresses.toArray()
		]
	}

	void "return 200 if valid subscriber"() {
		setup:
		controller.streamService = streamService = Mock(StreamService)
		String address = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		when:
		params.id = streamOne.id
		params.address = address
		request.method = "GET"
		authenticatedAs(me) { controller.subscriber() }

		then:
		1 * streamService.isStreamEthereumSubscriber(streamOne, address) >> true
		response.status == 200
	}

	void "return 404 if invalid subscriber"() {
		setup:
		controller.streamService = streamService = Mock(StreamService)
		String address = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		when:
		params.id = streamOne.id
		params.address = address
		request.method = "GET"
		authenticatedAs(me) { controller.subscriber() }

		then:
		1 * streamService.isStreamEthereumSubscriber(streamOne, address) >> false
		response.status == 404
	}
}
