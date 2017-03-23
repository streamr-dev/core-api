package com.unifina.controller.data

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.feed.NoOpStreamListener
import com.unifina.service.StreamService
import com.unifina.service.PermissionService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(StreamController)
@Mock([SecUser, Stream, Feed, Module, PermissionService, StreamService])
class StreamControllerSpec extends Specification {

	Feed feed
	SecUser user
	Stream stream
	Module module

	void setup() {
		module = new Module(id: 1).save(validate: false)
		feed = new Feed(streamListenerClass: NoOpStreamListener.name, module: module).save(validate: false)
		user = new SecUser(username: "me", password: "foo").save(validate:false)

		stream = new Stream(name: "dummy", description: "dummy", feed: feed)
		stream.id = "dummy"
		stream.save(validate: false)

		mockSpringSecurityService(user)
		controller.streamService = grailsApplication.mainContext.getBean("streamService")
		controller.permissionService = Stub(PermissionService) { getAll(*_) >> [stream] }

	}

	private void mockSpringSecurityService(newUser) {
		def springSecurityService = [
			getCurrentUser: {-> newUser },
			encodePassword: {String pw -> pw+"-encoded" }
		] as SpringSecurityService
		controller.springSecurityService = springSecurityService
	}

	void "stream create form"() {
		when:
		params.name = "Test stream"
		params.description = "Test stream"
		params.feed = feed.id
		params.format = "html"
		request.method = 'POST'
		controller.create()
		then:
        response.redirectedUrl == '/stream/show/' + Stream.list()[1].id
		Stream.count() == 2
		Stream.list()[1].user == user
	}

	void "searching for a stream returns correct module"() {
		when:
		params.term = stream.name[1..2]
		controller.search()
		then:
		response.json.size() == 1
		response.json[0].id == stream.id
		response.json[0].name == stream.name
		response.json[0].description == stream.description
		response.json[0].module == module.id
	}

}
