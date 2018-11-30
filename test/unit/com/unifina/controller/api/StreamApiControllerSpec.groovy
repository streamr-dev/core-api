package com.unifina.controller.api

import com.unifina.ControllerSpecification
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
		request.json = '{name: "newName", description: "newDescription"}'
		authenticatedAs(me) { controller.update() }

		then:
		response.status == 204

		then:
		def stream = streamOne
		stream.name == "newName"
		stream.description == "newDescription"
		stream.config == null
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

	void "returns set of producer addresses"() {
		setup:
		controller.streamService = streamService = Mock(StreamService)
		Set<String> addresses = new HashSet<String>()
		addresses.add('0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6')
		addresses.add('0x0181ae2f5efe8947eca8c2e9d3f32702cf4be7dd')
		when:
		params.id = streamOne.id
		request.method = "GET"
		authenticatedAs(me) { controller.producers() }

		then:
		1 * streamService.getStreamEthereumProducers(streamOne) >> addresses
		response.status == 200
		response.json == [
		    'addresses': addresses.toArray()
		]
	}
}
