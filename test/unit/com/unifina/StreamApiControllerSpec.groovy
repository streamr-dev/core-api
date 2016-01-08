package com.unifina

import com.unifina.controller.api.StreamApiController
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.service.KafkaService
import com.unifina.service.StreamService
import com.unifina.service.UnifinaSecurityService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import com.unifina.filters.UnifinaCoreAPIFilters
import spock.lang.Specification

@TestFor(StreamApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([SecUser, Stream, Feed, UnifinaCoreAPIFilters, UnifinaSecurityService, SpringSecurityService, StreamService])
class StreamApiControllerSpec extends Specification {

	SecUser user

	def streamService
	def unifinaSecurityService

	def setup() {
		streamService = mainContext.getBean(StreamService)
		unifinaSecurityService = mainContext.getBean(UnifinaSecurityService)

		controller.streamService = streamService
		controller.streamService.kafkaService = Mock(KafkaService)
		controller.unifinaSecurityService = unifinaSecurityService

		user = new SecUser(username: "me", password: "foo", apiKey: "apiKey")
		user.save(validate: false)

		def otherUser = new SecUser(username: "other", password: "bar", apiKey: "otherApiKey").save(validate: false)

		streamService.createUserStream([name: "stream", description: "description"], user, null)
		streamService.createUserStream([name: "ztream"], user, null)
		streamService.createUserStream([name: "atream"], user, null)
		streamService.createUserStream([name: "otherUserStream"], otherUser, null)
	}

	void "find all streams of logged in user"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		request.method = "GET"
		request.requestURI = "/api/v1/stream"
		withFilters([action: 'index']) {
			controller.index()
		}

		then:
		response.json.length() == 3
	}

	void "find streams by name of logged in user"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		request.name = "stream"
		request.method = "GET"
		request.requestURI = "/api/v1/stream"
		withFilters([action: 'index']) {
			controller.index()
		}

		then:
		response.json.length() == 1
		response.json[0].id > 0
		response.json[0].uuid.length() == 22
		response.json[0].apiKey.length() == 22
		response.json[0].name == "stream"
		response.json[0].config == [
		    topic: response.json[0].uuid,
			fields: []
		]
		response.json[0].description == "description"
	}

	void "create a new stream for currently logged in user"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		request.json = [name: "Test stream", description: "Test stream"]
		request.method = 'POST'
		request.requestURI = '/api/v1/stream/create' // UnifinaCoreAPIFilters has URI-based matcher
		withFilters([action:'save']) {
			controller.save()
		}
		then:
		response.json.id > 0
		response.json.uuid.length() == 22
		response.json.apiKey.length() == 22
		response.json.name == "Test stream"
		response.json.config == [
			topic: response.json.uuid,
			fields: []
		]
		response.json.description == "Test stream"
		Stream.count() == 5
		Stream.findById(response.json.id).user == user
	}

	void "create a new stream (with fields) for currently logged in user"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		request.json = [
			name: "Test stream",
			description: "Test stream",
			config: [
				fields: [
					[name: "profit", type: "number"],
					[name: "keyword", type: "string"]
				]
			]
		]
		request.method = 'POST'
		request.requestURI = '/api/v1/stream/create' // UnifinaCoreAPIFilters has URI-based matcher
		withFilters([action:'save']) {
			controller.save()
		}
		then:
		response.json.id > 0
		response.json.uuid.length() == 22
		response.json.apiKey.length() == 22
		response.json.name == "Test stream"
		response.json.config == [
			topic: response.json.uuid,
			fields: [
				[name: "profit", type: "number"],
				[name: "keyword", type: "string"]
			]
		]
		response.json.description == "Test stream"
		Stream.count() == 5
		Stream.findById(response.json.id).user == user
	}

	void "creating stream fails given invalid token"() {
		when:
		request.addHeader("Authorization", "Token wrongKey")
		request.json = [name: "Test stream", description: "Test stream"]
		request.method = 'POST'
		request.requestURI = '/api/v1/stream/create'
		withFilters([action:'save']) {
			controller.save()
		}
		then:
		response.json.success == false
		response.status == 401
	}

	void "creating stream fails when required parameters are missing"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		request.method = 'POST'
		request.requestURI = '/api/v1/stream/create'
		withFilters([action:'save']) {
			controller.save()
		}
		then:
		response.json.success == false
		response.status == 400
	}

	void "show a Stream of logged in user"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		params.id = 1
		request.method = "GET"
		request.requestURI = "/api/v1/stream"
		withFilters([action: "show"]) {
			controller.show()
		}

		then:
		response.status == 200
		response.json.id == 1
		response.json.name == "stream"
	}

	void "cannot shown non-existent Stream"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		params.id = 666
		request.method = "GET"
		request.requestURI = "/api/v1/stream"
		withFilters([action: "show"]) {
			controller.show()
		}

		then:
		response.status == 404
	}

	void "cannot show other user's Stream"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		params.id = 4
		request.method = "GET"
		request.requestURI = "/api/v1/stream"
		withFilters([action: "show"]) {
			controller.show()
		}

		then:
		response.status == 403
	}

	void "update a Stream of logged in user"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		params.id = 1
		request.method = "PUT"
		request.json = '{name: "newName", description: "newDescription"}'
		request.requestURI = "/api/v1/stream"
		withFilters([action: "update"]) {
			controller.update()
		}

		then:
		response.status == 204

		then:
		def stream = Stream.findById(1)
		stream.name == "newName"
		stream.description == "newDescription"
		stream.config != null
	}

	void "cannot update non-existent Stream"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		params.id = 666
		request.method = "PUT"
		request.json = '{name: "newName", description: "newDescription"}'
		request.requestURI = "/api/v1/stream"
		withFilters([action: "update"]) {
			controller.update()
		}

		then:
		response.status == 404
	}

	void "cannot update other user's Stream"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		params.id = 4
		request.method = "PUT"
		request.json = '{name: "newName", description: "newDescription"}'
		request.requestURI = "/api/v1/stream"
		withFilters([action: "update"]) {
			controller.update()
		}

		then:
		response.status == 403
	}

	void "delete a Stream of logged in user"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		params.id = 1
		request.method = "DELETE"
		request.requestURI = "/api/v1/stream"
		withFilters([action: "delete"]) {
			controller.delete()
		}

		then:
		response.status == 204
	}

	void "cannot delete non-existent Stream"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		params.id = 666
		request.method = "DELETE"
		request.requestURI = "/api/v1/stream"
		withFilters([action: "delete"]) {
			controller.delete()
		}

		then:
		response.status == 404
	}

	void "cannot delete other user's Stream"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}")
		params.id = 4
		request.method = "DELETE"
		request.requestURI = "/api/v1/stream"
		withFilters([action: "delete"]) {
			controller.delete()
		}

		then:
		response.status == 403
	}
}
