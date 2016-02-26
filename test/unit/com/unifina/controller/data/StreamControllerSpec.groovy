package com.unifina.controller.data

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.NoOpStreamListener
import com.unifina.service.StreamService
import com.unifina.service.UnifinaSecurityService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(StreamController)
@Mock([SecUser, Stream, Feed, UnifinaSecurityService, StreamService])
class StreamControllerSpec extends Specification {

	Feed feed
	SecUser user
	
	void setup() {
		// Mock services or use real ones
		controller.streamService = grailsApplication.mainContext.getBean("streamService")
		controller.unifinaSecurityService = grailsApplication.mainContext.getBean("unifinaSecurityService")
		
		SpringSecurityService springSecurityService = mockSpringSecurityService(null)

		feed = new Feed(streamListenerClass: NoOpStreamListener.name).save(validate: false)

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
			params.feed = feed.id
			params.format = "html"
			request.method = 'POST'
			controller.create()
		then:
			response.redirectedUrl == '/stream/show/1'
			Stream.count() == 1
			Stream.list()[0].user == user
	}

}
