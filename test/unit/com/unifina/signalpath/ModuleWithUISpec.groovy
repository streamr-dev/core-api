package com.unifina.signalpath

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.NoOpStreamListener
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import com.unifina.utils.GlobalsFactory
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Holders
import spock.lang.Specification

import java.security.AccessControlException

@TestMixin(GrailsUnitTestMixin)
@Mock([Stream, Feed])
class ModuleWithUISpec extends Specification {

	ModuleWithUI module
	PermissionService permissionService
	StreamService streamService
	SecUser permittedUser = new SecUser()
	SecUser nonPermitterUser = new SecUser()

	def setup() {
		module = new ModuleWithUI() {
			@Override
			void sendOutput() {
				
			}
			@Override
			void clearState() {
				
			}

			@Override
			String getUiChannelName() {
				return "TestModule"
			}

			@Override
			String getWebcomponentName() {
				return "webcomponent-name"
			}
		}

		streamService = Mock(StreamService)
		streamService.getStream("uiChannel-id") >> {
			Stream s = new Stream()
			s.id = "uiChannel-id"
			return s
		}
		Holders.getApplicationContext().beanFactory.registerSingleton('streamService', streamService)

		permissionService = Mock(PermissionService)
		permissionService.canWrite(permittedUser, _) >> true
		permissionService.canWrite(nonPermitterUser, _) >> false
		Holders.getApplicationContext().beanFactory.registerSingleton('permissionService', permissionService)

		Feed feed = new Feed()
		feed.id = Feed.KAFKA_ID
		feed.streamListenerClass = NoOpStreamListener.getName()
		feed.save(validate:false)
	}

	def cleanup() {
		Holders.getApplicationContext().beanFactory.destroySingleton("streamService")
		Holders.getApplicationContext().beanFactory.destroySingleton("permissionService")
	}

	def "onConfiguration must create an ui channel if an id is not configured"() {
		module.globals = GlobalsFactory.createInstance([:], grailsApplication, permittedUser)
		module.init()

		when:
		module.onConfiguration([:])
		then:
		1 * streamService.createStream(_, permittedUser) >> {
			Stream s = new Stream()
			s.id = "fooid"
			return s
		}
		module.getUiChannelId() == "fooid"
		module.getConfiguration().uiChannel != null
		module.getConfiguration().uiChannel.id == "fooid"
		module.getConfiguration().uiChannel.name == "TestModule"
		module.getConfiguration().uiChannel.webcomponent == module.webcomponentName
	}

	def "onConfiguration must get the stream if it exists"() {
		module.globals = GlobalsFactory.createInstance([:], grailsApplication, permittedUser)
		module.init()

		when:
		module.onConfiguration([uiChannel:[id:'uiChannel-id']])
		then:
		module.getUiChannelStream().id == 'uiChannel-id'
	}

	def "onConfiguration must throw if the defined Stream is not found"() {
		module.globals = GlobalsFactory.createInstance([:], grailsApplication, permittedUser)
		module.init()

		when:
		module.onConfiguration([uiChannel:[id:'notfound']])
		then:
		thrown(IllegalStateException)
	}

	def "onConfiguration must not allow users to write to streams they don't have write permission to"() {
		module.globals = GlobalsFactory.createInstance([:], grailsApplication, nonPermitterUser)
		module.init()

		when:
		module.onConfiguration([uiChannel:[id:'uiChannel-id']])
		then:
		thrown(AccessControlException)
	}

	def "pushToUiChannel must send the message via streamService"() {
		module.globals = GlobalsFactory.createInstance([:], grailsApplication, permittedUser)
		module.init()
		module.onConfiguration([uiChannel:[id:'uiChannel-id']])

		Map msg = [foo: "bar"]
		
		when:
			module.pushToUiChannel(msg)
		then:
			1 * streamService.sendMessage(module.getUiChannelStream(), msg)
	}
}
