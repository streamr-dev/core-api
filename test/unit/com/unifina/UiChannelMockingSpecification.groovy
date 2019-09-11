package com.unifina

import com.streamr.client.protocol.message_layer.StreamMessage
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

/**
 * Spec base class with helper functions for testing ModuleWithUI-based modules.
 */
@TestMixin(GrailsUnitTestMixin)
class UiChannelMockingSpecification extends ModuleTestingSpecification {

	Map<String, List<Map>> sentMessagesByStreamId = [:]

	protected void mockServicesForUiChannels(Canvas canvas = new Canvas()) {
		StreamService streamService = mockBean(StreamService, Mock(StreamService))

		streamService.getStream(_) >> {String streamId->
			Stream s = new Stream()
			s.id = streamId
			s.name = streamId
			s.uiChannel = true
			s.uiChannelCanvas = canvas
			return s
		}
		streamService.sendMessage(_) >> {StreamMessage msg->
			String c = msg.getStreamId()
			if (!sentMessagesByStreamId.containsKey(c)) {
				sentMessagesByStreamId.put(c, new ArrayList<Map>())
			}
			sentMessagesByStreamId.get(c).add(msg.getContent())
		}
		streamService.createStream(_, _, _) >> { Map params, SecUser user, String id->
			Stream s = new Stream(params)
			s.id = id
			return s
		}

		PermissionService permissionService = Mock(PermissionService)
		mockBean(PermissionService, permissionService)

		permissionService.canRead(_, _) >> true
		permissionService.canWrite(_, _) >> true
		permissionService.canShare(_, _) >> true
	}
}
