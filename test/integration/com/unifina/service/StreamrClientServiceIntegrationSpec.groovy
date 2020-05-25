package com.unifina.service

import com.streamr.client.StreamrClient
import com.unifina.utils.testutils.FakeStreamrClient
import grails.test.spock.IntegrationSpec

class StreamrClientServiceIntegrationSpec extends IntegrationSpec {

	StreamrClientService streamrClientService

	void "getInstanceForThisEngineNode() is able to retrieve a sessionToken without making API calls"() {
		streamrClientService.setClientClass(FakeStreamrClient)

		StreamrClient client
		String sessionToken

		when:
		client = streamrClientService.getInstanceForThisEngineNode()
		sessionToken = client.getSessionToken()

		then:
		sessionToken != null

		cleanup:
		client?.disconnect() // the client shouldn't be connected, but defend against future changes
	}
}
