package com.unifina.controller.data

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.service.StreamService
import com.unifina.service.UnifinaSecurityService
import com.unifina.utils.IdGenerator;

@TestFor(StreamController)
@Mock([SecUser, Stream, Feed])
class StreamControllerSpec extends Specification {

	def springSecurityService = [
		encodePassword: { pw -> return pw+"-encoded" },
		currentUser: null
	]
	
	SecUser user
	
	void setup() {
		defineBeans {
			unifinaSecurityService(UnifinaSecurityService)
			streamService(StreamService)
		}
		
		controller.springSecurityService = springSecurityService
		controller.unifinaSecurityService.springSecurityService = springSecurityService
		
		// Users
		user = new SecUser(username: "me", password: "foo", apiKey: "apiKey", apiSecret: "apiSecret")
		user.save(validate:false)
		
	}

	void "successful stream create json api call"() {
		when:
			request.json = [key: user.apiKey, secret: user.apiSecret, name: "Test stream", description: "Test stream", localId: "my-stream-id"]
			request.method = 'POST'
			controller.apiCreate()
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
			request.json = [key: user.apiKey, secret: "wrong secret", name: "Test stream", description: "Test stream", localId: "my-stream-id"]
			request.method = 'POST'
			controller.apiCreate()
		then:
			response.json.success == false
			response.status == 401
	}
	
	void "invalid stream create json api call values"() {
		when:
			request.json = [key: user.apiKey, secret: user.apiSecret]
			request.method = 'POST'
			controller.apiCreate()
		then:
			response.json.success == false
			response.status == 400
	}
	
	void "stream create form"() {
		springSecurityService.currentUser = user
		
		when:
			params.name = "Test stream"
			params.description = "Test stream"
			params.format = "html"
			request.method = 'POST'
			controller.create()
		then:
			response.redirectedUrl == '/stream/show/1'
			Stream.count() == 1
			Stream.list()[0].user == user
	}
	
	void "successful stream lookup by localId"() {
		Stream stream = controller.streamService.createUserStream([name:"Test",description:"Test desc",localId:"localId"], user)
		
		when:
			request.json = [key: user.apiKey, secret: user.apiSecret, localId: stream.localId]
			request.method = 'POST'
			controller.apiLookup()
		then:
			response.status == 200
			response.json.stream == stream.uuid
	}
	
	void "unsuccessful stream lookup by localId"() {
		Stream stream = controller.streamService.createUserStream([name:"Test",description:"Test desc",localId:"localId"], user)
		
		when:
			request.json = [key: user.apiKey, secret: user.apiSecret, localId: "wrong local id"]
			request.method = 'POST'
			controller.apiLookup()
		then:
			response.status == 404
			response.json.success == false
	}

}
