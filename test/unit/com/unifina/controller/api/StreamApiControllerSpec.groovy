package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.BadRequestException
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.StreamListParams
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.feed.NoOpStreamListener
import com.unifina.service.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import java.text.SimpleDateFormat

@TestFor(StreamApiController)
@Mock([SecUser, Stream, Key, Permission, Feed, PermissionService, StreamService, DashboardService])
class StreamApiControllerSpec extends ControllerSpecification {

	Feed feed
	SecUser me

	def streamService
	def permissionService
	def apiService

	Stream streamOne
	def streamTwoId
	def streamThreeId
	def streamFourId

	def setup() {
		permissionService = mainContext.getBean(PermissionService)

		controller.permissionService = permissionService
		apiService = controller.apiService = Mock(ApiService)

		me = new SecUser(username: "me", password: "foo")
		me.save(validate: false)

		Key key = new Key(name: "key", user: me)
		key.id = "apiKey"
		key.save(failOnError: true, validate: true)

		feed = new Feed(streamListenerClass: NoOpStreamListener.name, id: 7).save(validate: false)

		def otherUser = new SecUser(username: "other", password: "bar").save(validate: false)

		// First use real streamService to create the streams
		streamService = mainContext.getBean(StreamService)
		streamService.permissionService = permissionService
		streamOne = streamService.createStream([name: "stream", description: "description", feed: feed], me)
		streamTwoId = streamService.createStream([name: "ztream", feed: feed], me).id
		streamThreeId = streamService.createStream([name: "atream", feed: feed], me).id
		streamFourId = streamService.createStream([name: "otherUserStream", feed: feed], otherUser).id

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
		request.json = [name: "Test stream", description: "Test stream", feed: feed]
		request.method = 'POST'
		unauthenticated() { controller.save() }

		then:
		response.status == 401
	}

	void "save() calls StreamService.createStream() and returns it.toMap()"() {
		setup:
		controller.streamService = streamService = Mock(StreamService)
		def stream = new Stream(feed: new Feed())
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
		thrown NotPermittedException
	}

	void "shows a Stream of logged in Key"() {
		Key key = new Key(name: "anonymous key")
		key.id = "anonymousKeyKey"
		key.save(failOnError: true)
		permissionService.systemGrant(key, streamOne, Permission.Operation.READ)

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

	void "update a Stream of logged in user"() {
		when:
		params.id = streamOne.id
		request.method = "PUT"
		request.json = '{name: "newName", description: "newDescription", autoConfigure: false, requireSignedData: true, storageDays: 24 }'
		authenticatedAs(me) { controller.update() }

		then:
		response.status == 204

		then:
		def stream = streamOne
		stream.name == "newName"
		stream.description == "newDescription"
		stream.config == null
		stream.autoConfigure == false
		stream.requireSignedData == true
		stream.storageDays == 24
	}

	void "update a Stream of logged in user but do not update undefined fields"() {
		when:
		params.id = streamOne.id
		request.method = "PUT"
		request.json = '{name: "newName", description: "newDescription", autoConfigure: null, requireSignedData: null, storageDays: null }'
		authenticatedAs(me) { controller.update() }

		then:
		response.status == 204

		then:
		def stream = streamOne
		stream.name == "newName"
		stream.description == "newDescription"
		stream.config == null
		stream.autoConfigure == true
		stream.requireSignedData == false
		stream.storageDays == Stream.DEFAULT_STORAGE_DAYS
	}

	void "cannot update non-existent Stream"() {
		when:
		params.id = "666-666-666"
		request.method = "PUT"
		request.json = '{name: "newName", description: "newDescription"}'
		authenticatedAs(me) { controller.update() }

		then:
		thrown NotFoundException
	}

	void "cannot update other user's Stream"() {
		when:
		params.id = streamFourId
		request.method = "PUT"
		request.json = '{name: "newName", description: "newDescription"}'
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

	void "streams status"() {
		setup:
		controller.streamService = Mock(StreamService)
		Date timestamp = newDate(2019, 1, 19, 2, 0, 3)

		when:
		params.days = 2
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
		params.days = 2
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
		params.days = 2
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

	void "deleteDataUpTo deletes stream's data up to given date"() {
		controller.streamService = Mock(StreamService)
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2019-05-09")

		when:
		request.method = "DELETE"
		params.id = streamOne.id
		params.date = "2019-05-09"
		authenticatedAs(me) { controller.deleteDataUpTo() }

		then:
		1 * controller.streamService.deleteDataUpTo(streamOne, date)
		response.status == 204
	}

	void "deleteDataUpTo responds with 400 on bad date input"() {
		controller.streamService = Mock(StreamService)

		when:
		request.method = "DELETE"
		params.id = streamOne.id
		params.date = "2019-xx-xx"
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

	void "deleteDataRange deletes streams data from a given date range"() {
		controller.streamService = Mock(StreamService)
		Date start = new SimpleDateFormat("yyyy-MM-dd").parse("2019-05-01")
		Date end = new SimpleDateFormat("yyyy-MM-dd").parse("2019-05-30")
		when:
		request.method = "DELETE"
		params.id = streamOne.id
		params.start = "2019-05-01"
		params.end = "2019-05-30"
		authenticatedAs(me) { controller.deleteDataRange() }

		then:
		1 * controller.streamService.deleteDataRange(streamOne, start, end)
		response.status == 204
	}

	void "deleteDataRange responds with 400 when start date is bad"() {
		controller.streamService = Mock(StreamService)
		when:
		request.method = "DELETE"
		params.id = streamOne.id
		params.start = "2019-xx-xx"
		params.end = "2019-05-30"
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
		params.start = "2019-05-01"
		params.end = "2019-xx-xx"
		authenticatedAs(me) { controller.deleteDataRange() }

		then:
		0 * controller.streamService._
		thrown(BadRequestException)
	}
}
