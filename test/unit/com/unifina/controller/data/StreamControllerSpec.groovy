package com.unifina.controller.data

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.service.KafkaService
import com.unifina.service.StreamService
import com.unifina.service.UnifinaSecurityService

@TestFor(StreamController)
@Mock([SecUser, Stream, Feed, UnifinaSecurityService, StreamService])
class StreamControllerSpec extends Specification {
	
	SecUser user
	
	void setup() {
		// Mock services or use real ones
		controller.streamService = grailsApplication.mainContext.getBean("streamService")
		controller.streamService.kafkaService = Mock(KafkaService)
		controller.unifinaSecurityService = grailsApplication.mainContext.getBean("unifinaSecurityService")
		
		SpringSecurityService springSecurityService = mockSpringSecurityService(null)
		
		// Users
		user = new SecUser(username: "me", password: "foo", apiKey: "apiKey")
		user.save(validate:false)
		
	}
	
	private void mockSpringSecurityService(user) {
		def springSecurityService = [
			getCurrentUser: {-> user },
			encodePassword: {String pw-> pw+"-encoded" }
		] as SpringSecurityService
		controller.springSecurityService = springSecurityService
		controller.unifinaSecurityService.springSecurityService = springSecurityService
	}
	
	void "stream create form"() {
		mockSpringSecurityService(user)
		
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

}
