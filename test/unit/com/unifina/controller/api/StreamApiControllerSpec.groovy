package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.BadRequestException
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.StreamListParams
import com.unifina.api.ValidationException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.service.*
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import java.text.SimpleDateFormat

@TestFor(StreamApiController)
@Mock([SecUser, Stream, Key, Permission, PermissionService, StreamService, DashboardService, IntegrationKey])
class StreamApiControllerSpec extends ControllerSpecification {

	SecUser me
	Key key

	StreamService streamService
	PermissionService permissionService
	ApiService apiService

	Stream streamOne
	def streamTwoId
	def streamThreeId
	def streamFourId

	SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

	def setup() {
		permissionService = mainContext.getBean(PermissionService)

		controller.permissionService = permissionService
		apiService = controller.apiService = Mock(ApiService)

		me = new SecUser(username: "me", password: "foo")
		me.save(validate: false)

		key = new Key(name: "key", user: me)
		key.id = "apiKey"
		key.save(failOnError: true, validate: true)

		def otherUser = new SecUser(username: "other", password: "bar").save(validate: false)

		// First use real streamService to create the streams
		streamService = mainContext.getBean(StreamService)
		streamService.permissionService = permissionService
		streamService.cassandraService = mockBean(CassandraService, Mock(CassandraService))
		streamOne = streamService.createStream([name: "stream", description: "description"], me)
		streamTwoId = streamService.createStream([name: "ztream"], me).id
		streamThreeId = streamService.createStream([name: "atream"], me).id
		streamFourId = streamService.createStream([name: "otherUserStream"], otherUser).id

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
		response.json[0].id.length() == 22
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
		request.json = [test: "test"]
		request.method = 'POST'
		authenticatedAs(me) { controller.save() }

		then:
		1 * streamService.createStream([test: "test"], me) >> { stream }
		response.json.id == stream.toMap().id
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

	void "shows a Stream of logged in Key"() {
		Key key = new Key(name: "anonymous key")
		key.id = "anonymousKeyKey"
		key.save(failOnError: true)
		permissionService.systemGrant(key, streamOne, Permission.Operation.STREAM_GET)

		when:
		params.id = streamOne.id
		authenticatedAs(me) { controller.show() }

		then:
		response.status == 200
		response.json.name == "stream"
	}

	void "does not show Stream if key not permitted"() {
		Key key = new Key(name: "anonymous key")
		key.id = "anonymousKeyKey"
		key.save(failOnError: true)

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
		1 * streamService.cassandraService.deleteAll(streamOne)
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
		authenticatedAs(me) { controller.setFields()}

		then:
		1 * apiService.authorizedGetById(Stream, streamOne.id, me, Permission.Operation.STREAM_EDIT) >> streamOne
		streamOne.config == '{"fields":{"field1":"string"}}'
		response.status == 200
	}

	void "can set fields with key"() {
		when:
		params.id = streamOne.id
		request.method = "POST"
		request.JSON = ["field1": "string"]
		authenticatedAs(key) { controller.setFields()}

		then:
		1 * apiService.authorizedGetById(Stream, streamOne.id, key, Permission.Operation.STREAM_EDIT) >> streamOne
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

	void "streams status"() {
		setup:
		controller.streamService = Mock(StreamService)
		Date timestamp = newDate(2019, 1, 19, 2, 0, 3)

		when:
		params.id = streamOne.id
		request.method = "GET"
		authenticatedAs(me) { controller.status() }

		then:
		1 * controller.streamService.status(_, _) >> new StreamService.StreamStatus(true, timestamp)
		response.status == 200
		response.json == [
		    ok: true,
			date: "2019-01-19T02:00:03Z",
		]
	}

	void "streams status no message"() {
		setup:
		controller.streamService = Mock(StreamService)

		when:
		params.id = streamOne.id
		request.method = "GET"
		authenticatedAs(me) { controller.status() }

		then:
		1 * controller.streamService.status(_, _) >> new StreamService.StreamStatus(false, null)
		response.status == 200
		response.json == [
			ok: false,
		]
	}

	void "stream status not found"() {
		setup:
		controller.streamService = Mock(StreamService)

		when:
		params.id = "not-found"
		request.method = "GET"
		authenticatedAs(me) { controller.status() }

		then:
		0 * controller.streamService._
		thrown NotFoundException
		response.status == 404
	}

	Date newDate(int year, int month, int date, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance()
		cal.set(Calendar.YEAR, year)
		cal.set(Calendar.MONTH, month - 1)
		cal.set(Calendar.DATE, date)
		cal.set(Calendar.HOUR_OF_DAY, hour)
		cal.set(Calendar.MINUTE, minute)
		cal.set(Calendar.SECOND, second)
		return cal.getTime()
	}

	// sample dates for testing
	// 1557478677036 -> 2019-05-10T08:57:57Z
	// 1557479167606 -> 2019-05-10T09:06:07Z

	void "deleteDataUpTo deletes stream's data up to given date"() {
		controller.streamService = Mock(StreamService)
		Date date = iso8601.parse("2019-05-10T08:57:57Z")

		when:
		request.method = "DELETE"
		params.id = streamOne.id
		request.JSON.date = "2019-05-10T08:57:57Z"
		authenticatedAs(me) { controller.deleteDataUpTo() }

		then:
		1 * controller.streamService.deleteDataUpTo(streamOne, date)
		response.status == 204
	}

	void "deleteDataUpTo deletes stream's data up to given date in milliseconds"() {
		controller.streamService = Mock(StreamService)

		when:
		request.method = "DELETE"
		params.id = streamOne.id
		request.JSON.date = 1557478677036
		authenticatedAs(me) { controller.deleteDataUpTo() }

		then:
		1 * controller.streamService.deleteDataUpTo(streamOne, new Date(1557478677036))
		response.status == 204
	}

	void "deleteDataUpTo responds with 400 on bad date input"() {
		controller.streamService = Mock(StreamService)

		when:
		request.method = "DELETE"
		params.id = streamOne.id
		request.JSON.date = "2019-05-10T08:57:xxZ"
		authenticatedAs(me) { controller.deleteDataUpTo() }

		then:
		0 * controller.streamService._
		thrown(BadRequestException)
	}

	void "deleteAllData deletes all data from a given stream"() {
		controller.streamService = Mock(StreamService)

		when:
		request.method = "DELETE"
		params.id = streamOne.id
		authenticatedAs(me) { controller.deleteAllData() }

		then:
		1 * controller.streamService.deleteAllData(streamOne)
		response.status == 204
	}

	void "deleteDataRange deletes streams data from a given date range in milliseconds"() {
		controller.streamService = Mock(StreamService)
		when:
		request.method = "DELETE"
		params.id = streamOne.id
		request.JSON.start = 1557478677036
		request.JSON.end = 1557479167606
		authenticatedAs(me) { controller.deleteDataRange() }

		then:
		1 * controller.streamService.deleteDataRange(streamOne, new Date(1557478677036), new Date(1557479167606))
		response.status == 204
	}

	void "deleteDataRange responds with 400 when start date is bad"() {
		controller.streamService = Mock(StreamService)
		when:
		request.method = "DELETE"
		params.id = streamOne.id
		request.JSON.start = "2019-05-10T08:57:xxZ"
		request.JSON.end = "2019-05-10T09:06:07Z"
		authenticatedAs(me) { controller.deleteDataRange() }

		then:
		0 * controller.streamService._
		thrown(BadRequestException)
	}

	void "deleteDataRange responds with 400 when end date is bad"() {
		controller.streamService = Mock(StreamService)
		when:
		request.method = "DELETE"
		params.id = streamOne.id
		request.JSON.start = "2019-05-10T08:57:57Z"
		request.JSON.end = "2019-05-10T09:06:xxZ"
		authenticatedAs(me) { controller.deleteDataRange() }

		then:
		0 * controller.streamService._
		thrown(BadRequestException)
	}

	void "deleteDataRange deletes streams data from a given date range"() {
		controller.streamService = Mock(StreamService)
		Date start = iso8601.parse("2019-05-10T08:57:57Z")
		Date end = iso8601.parse("2019-05-10T09:06:07Z")
		when:
		request.method = "DELETE"
		params.id = streamOne.id
		request.JSON.start = "2019-05-10T08:57:57Z"
		request.JSON.end = "2019-05-10T09:06:07Z"
		authenticatedAs(me) { controller.deleteDataRange() }

		then:
		1 * controller.streamService.deleteDataRange(streamOne, start, end)
		response.status == 204
	}

	void "detectFields GET"() {
		setup:
		controller.streamService = Mock(StreamService)
		streamOne.config = ([
			fields: [
				[name: "foo", type: "number"],
				[name: "bar", type: "boolean"],
			],
		] as JSON)

		when:
		request.method = "GET"
		params.id = streamOne.id
		params.flatten = false
		authenticatedAs(me) { controller.detectFields() }

		then:
		1 * controller.streamService.autodetectFields(streamOne, false, false) >> true
		response.status == 200
		response.json.id == streamOne.id
		response.json.name == "stream"
		response.json.description == "description"
		response.json.config.fields[0].name == "foo"
		response.json.config.fields[0].type == "number"
		response.json.config.fields[1].name == "bar"
		response.json.config.fields[1].type == "boolean"
	}

	void "detectFields POST"() {
		setup:
		controller.streamService = Mock(StreamService)
		streamOne.config = ([
			fields: [
				[name: "foo", type: "number"],
				[name: "bar", type: "boolean"],
			],
		] as JSON)

		when:
		request.method = "POST"
		params.id = streamOne.id
		params.flatten = false
		authenticatedAs(me) { controller.detectFields() }

		then:
		1 * controller.streamService.autodetectFields(streamOne, false, true) >> true
		response.status == 200
		response.json.id == streamOne.id
		response.json.name == "stream"
		response.json.description == "description"
		response.json.config.fields[0].name == "foo"
		response.json.config.fields[0].type == "number"
		response.json.config.fields[1].name == "bar"
		response.json.config.fields[1].type == "boolean"
	}
}
