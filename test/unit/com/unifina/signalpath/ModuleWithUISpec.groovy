package com.unifina.signalpath

import com.streamr.client.StreamrClient
import com.unifina.ModuleTestingSpecification
import com.unifina.datasource.IStartListener
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import com.unifina.service.StreamrClientService
import com.unifina.utils.Globals
import grails.test.mixin.Mock

import java.security.AccessControlException

@Mock([Stream, SecUser])
class ModuleWithUISpec extends ModuleTestingSpecification {

	Stream uiChannel
	Canvas canvas
	ModuleWithUI module
	PermissionService permissionService
	StreamService streamService
	StreamrClient streamrClient
	SecUser permittedUser = new SecUser(username: 'permittedUser')
	SecUser nonPermitterUser = new SecUser(username: 'nonPermittedUser')

	def setup() {
		canvas = new Canvas(name: "canvas")
		canvas.id = "canvas-id-1"
		canvas.save(failOnError: true, validate: true)

		uiChannel = new Stream()
		uiChannel.name = "TestModule"
		uiChannel.id = "uiChannel-id"
		uiChannel.uiChannelCanvas = canvas

		streamService = mockBean(StreamService, Mock(StreamService))
		permissionService = mockBean(PermissionService, Mock(PermissionService))
		StreamrClientService streamrClientService = mockBean(StreamrClientService, Mock(StreamrClientService))

		streamrClient = Mock(StreamrClient)
		streamrClientService.getAuthenticatedInstance(_) >> streamrClient

		permittedUser.save(failOnError: true, validate: false)
		nonPermitterUser.save(failOnError: true, validate: false)
		permissionService.systemGrantAll(permittedUser, canvas)
	}

	private ModuleWithUI createModule(Map config, SecUser user) {
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
		module.globals = mockGlobals([:], user, Globals.Mode.REALTIME)
		module.globals.time = new Date()
		module.parentSignalPath = Mock(SignalPath)
		module.parentSignalPath.getRootSignalPath() >> module.parentSignalPath
		module.parentSignalPath.getRuntimePath(_) >> {RuntimeRequest.PathWriter writer ->
			return writer.writeCanvasId("id")
		}
		module.parentSignalPath.getCanvas() >> canvas
		module.init()
		module.setHash(1)
		module.onConfiguration(config)

		return module
	}

	def "onConfiguration must create an ui channel if an id is not configured"() {
		when:
		createModule([:], permittedUser)

		then: "config contains the id"
		module.getUiChannel().getId() != null
		module.getConfiguration().uiChannel != null
		module.getConfiguration().uiChannel.id != null
		module.getConfiguration().uiChannel.name == "TestModule"
		module.getConfiguration().uiChannel.webcomponent == module.webcomponentName
		and: "the Stream is not searched for, because the id was just generated and can't exist in the db"
		0 * streamService.getStream(_)
	}

	def "when the datasource starts, the Stream object is loaded if it exists"() {
		IStartListener listener

		when:
		createModule([uiChannel:[id:'uiChannel-id']], permittedUser)
		then:
		module.getUiChannel().getId() == 'uiChannel-id'

		when: "module is initialized"
		module.connectionsReady()
		then: "a start listener is registered"
		1 * module.globals.getDataSource().addStartListener(_) >> {IStartListener l->
			listener = l
		}

		when: "start listener is called"
		listener.onStart()
		then: "the Stream object is loaded"
		1 * streamService.getStream("uiChannel-id") >> uiChannel
		module.getUiChannel().getStream() == uiChannel
		and: "a new Stream is not created"
		1 * permissionService.check(permittedUser, uiChannel, Permission.Operation.STREAM_PUBLISH) >> true
		0 * streamService.createStream(_, _, _)
	}

	def "when the datasource starts, the Stream object is loaded by uiChannelPath if the parent SignalPath is not root"() {
		IStartListener listener
		uiChannel.id = "stream-loaded-by-uiChannelPath"

		when:
		createModule([uiChannel:[id:'nonexistent']], permittedUser)
		then:
		module.getUiChannel().getId() == 'nonexistent'

		when: "module is initialized"
		module.connectionsReady()
		then: "a start listener is registered"
		1 * module.globals.getDataSource().addStartListener(_) >> {IStartListener l->
			listener = l
		}

		when: "start listener is called"
		listener.onStart()
		then: "the Stream object load attempt fails"
		1 * streamService.getStream("nonexistent") >> null
		then: "the Stream object is loaded by uiChannelPath"
		1 * streamService.getStreamByUiChannelPath(_) >> uiChannel
		module.getUiChannel().getStream() == uiChannel
		module.getUiChannel().getId() == "stream-loaded-by-uiChannelPath"
		and: "a new Stream is not created"
		1 * permissionService.check(permittedUser, uiChannel, Permission.Operation.STREAM_PUBLISH) >> true
		0 * streamService.createStream(_, _, _)
	}

	def "when the datasource starts, the Stream object is created if it does not exist"() {
		IStartListener listener

		when:
		createModule([uiChannel:[id:'nonexistent']], permittedUser)
		then:
		module.getUiChannel().getId() == 'nonexistent'

		when: "module is initialized"
		module.connectionsReady()
		then: "a start listener is registered"
		1 * module.globals.getDataSource().addStartListener(_) >> {IStartListener l->
			listener = l
		}

		when: "start listener is called"
		listener.onStart()
		then: "the Stream object load attempt fails"
		1 * streamService.getStream("nonexistent") >> null
		then: "the Stream object is not found by uiChannelPath"
		1 * streamService.getStreamByUiChannelPath(_) >> null
		then: "the Stream object is created"
		1 * permissionService.check(permittedUser, uiChannel, Permission.Operation.STREAM_PUBLISH) >> true
		1 * streamService.createStream([name: uiChannel.name, uiChannel: true, uiChannelPath: "/canvases/id/modules/1", uiChannelCanvas: canvas], permittedUser, "nonexistent") >> uiChannel
	}

	def "users must not be allowed to write to ui channels for canvases they don't have write permission to"() {
		IStartListener listener
		createModule([uiChannel:[id:'uiChannel-id']], nonPermitterUser)

		when:
		module.connectionsReady()
		then: "a start listener is registered"
		1 * module.globals.getDataSource().addStartListener(_) >> {IStartListener l->
			listener = l
		}

		when: "start listener is called"
		listener.onStart()
		then: "the Stream object is loaded"
		1 * streamService.getStream("uiChannel-id") >> uiChannel
		1 * permissionService.check(_, _, Permission.Operation.STREAM_PUBLISH) >> false
		thrown(AccessControlException)
	}

	def "pushToUiChannel must send the message via streamService"() {
		IStartListener listener
		createModule([uiChannel:[id:'uiChannel-id']], permittedUser)
		Map msg = [foo: "bar"]

		when:
		module.connectionsReady()
		then: "a start listener is registered"
		1 * module.globals.getDataSource().addStartListener(_) >> {IStartListener l->
			listener = l
		}

		when: "start listener is called"
		listener.onStart()
		then: "the Stream object is loaded"
		1 * permissionService.check(permittedUser, uiChannel, Permission.Operation.STREAM_PUBLISH) >> true
		1 * streamService.getStream("uiChannel-id") >> uiChannel

		when:
		module.pushToUiChannel(msg)
		then:
		1 * streamrClient.publish(_, msg, _)
	}
}
