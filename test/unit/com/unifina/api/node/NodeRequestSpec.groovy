package com.unifina.api.node

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

class NodeRequestSpec extends Specification {
	void "throws MalformedURLException if given arguments do not add up to a proper URL"() {
		when:
		new NodeRequest("::@^^++", "++:://\\//", new MockHttpServletRequest(), new MockHttpServletResponse())
		then:
		thrown(MalformedURLException)
	}

	void "forms expected url"() {
		when:
		def request = new MockHttpServletRequest()
		request.serverPort = 443
		request.scheme = "https"
		def nodeRequest = new NodeRequest("6.6.6.6", "/api/v1/nodes/canvases", request, new MockHttpServletResponse())

		then:
		nodeRequest.url.toString() == "https://6.6.6.6:443/api/v1/nodes/canvases"
	}
}
