package com.unifina

import com.streamr.client.StreamrClient
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import com.unifina.service.StreamrClientService
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
		streamService.createStream(_, _, _) >> { Map params, SecUser user, String id->
			Stream s = new Stream(params)
			s.id = id
			return s
		}

		StreamrClient streamrClient = Mock(StreamrClient)
		streamrClient.getStream(_) >> {String streamId ->
			com.streamr.client.rest.Stream stream = new com.streamr.client.rest.Stream("mock stream name", "mock description")
			stream.setId(streamId)
			stream.setPartitions(1)
			return stream
		}
		streamrClient.publish(_, _, _) >> {com.streamr.client.rest.Stream stream, Map<String, Object> message, Date timestamp ->
			if (!sentMessagesByStreamId.containsKey(stream.getId())) {
				sentMessagesByStreamId.put(stream.getId(), new ArrayList<Map>())
			}
			sentMessagesByStreamId.get(stream.getId()).add(message)
		}

		StreamrClientService streamrClientService = mockBean(StreamrClientService, Mock(StreamrClientService))
		streamrClientService.getAuthenticatedInstance(_) >> streamrClient

		PermissionService permissionService = Mock(PermissionService)
		mockBean(PermissionService, permissionService)

		permissionService.check(_, _, _) >> true
		permissionService.check(_, _, _) >> true
		permissionService.check(_, _, _) >> true
	}
}
