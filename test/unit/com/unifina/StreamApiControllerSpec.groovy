package com.unifina

import com.unifina.controller.api.StreamApiController
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.service.KafkaService
import com.unifina.service.StreamService
import com.unifina.service.UnifinaSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import com.unifina.filters.UnifinaCoreAPIFilters
import spock.lang.Specification

@TestFor(StreamApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([SecUser, Stream, Feed, UnifinaCoreAPIFilters, UnifinaSecurityService, StreamService])
class StreamApiControllerSpec extends Specification {

	SecUser user

	def setup() {
		controller.streamService = mainContext.getBean(StreamService)
		controller.streamService.kafkaService = Mock(KafkaService)

		user = new SecUser(username: "me", password: "foo", apiKey: "apiKey", apiSecret: "apiSecret")
		user.save(validate: false)
	}

	void "successful stream create json api call"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}:${user.apiSecret}")
		request.json = [name: "Test stream", description: "Test stream", localId: "my-stream-id"]
		request.method = 'POST'
		request.requestURI = '/api/v1/streams/create' // UnifinaCoreAPIFilters has URI-based matcher
		withFilters([action:'create']) {
			controller.create()
		}
		then:
		response.json.success
		response.json.stream instanceof String
		response.json.auth instanceof String
		response.json.name == "Test stream"
		response.json.description == "Test stream"
		response.json.localId == "my-stream-id"
		Stream.count() == 1
		Stream.list()[0].user == user
	}

	void "invalid stream create json api call credentials"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}:wrongSecret")
		request.json = [name: "Test stream", description: "Test stream", localId: "my-stream-id"]
		request.method = 'POST'
		request.requestURI = '/api/v1/streams/create'
		withFilters([action:'create']) {
			controller.create()
		}
		then:
		response.json.success == false
		response.status == 401
	}

	void "invalid stream create json api call values"() {
		when:
		request.addHeader("Authorization", "Token ${user.apiKey}:${user.apiSecret}")
		request.method = 'POST'
		request.requestURI = '/api/v1/streams/create'
		withFilters([action:'create']) {
			controller.create()
		}
		then:
		response.json.success == false
		response.status == 400
	}


	void "successful stream lookup by localId"() {
		Stream stream = controller.streamService.createUserStream([name:"Test",description:"Test desc",localId:"localId"], user)

		when:
		request.addHeader("Authorization", "Token ${user.apiKey}:${user.apiSecret}")
		request.json = [localId: stream.localId]
		request.method = 'POST'
		request.requestURI = '/api/v1/streams/lookup'
		withFilters([action:'lookup']) {
			controller.lookup()
		}
		then:
		response.status == 200
		response.json.stream == stream.uuid
	}

	void "unsuccessful stream lookup by localId"() {
		Stream stream = controller.streamService.createUserStream([name:"Test",description:"Test desc",localId:"localId"], user)

		when:
		request.addHeader("Authorization", "Token ${user.apiKey}:${user.apiSecret}")
		request.json = [localId: "wrong local id"]
		request.method = 'POST'
		request.requestURI = '/api/v1/streams/lookup'
		withFilters([action:'lookup']) {
			controller.lookup()
		}
		then:
		response.status == 404
		response.json.success == false
	}
}
