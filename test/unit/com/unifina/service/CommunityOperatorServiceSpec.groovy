package com.unifina.service

import com.unifina.api.ProxyException
import grails.test.mixin.TestFor
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import spock.lang.Specification

import java.nio.charset.StandardCharsets

@TestFor(CommunityOperatorService)
class CommunityOperatorServiceSpec extends Specification {
	final String url = "http://localhost:8080/communities/"
	CloseableHttpResponse response
	HttpEntity entity
	StatusLine status

	void setup() {
		response = Mock(CloseableHttpResponse)
		entity = Mock(HttpEntity)
		status = Mock(StatusLine)
		service.client = Mock(HttpClient)
	}

	void "test execute()"() {
		setup:
		String expected = """{"result":[]}"""
		when:
		CommunityOperatorService.ProxyResponse result = service.proxy(url)
		then:
		1 * service.client.execute(_ as HttpGet) >> response
		1 * response.getStatusLine() >> status
		1 * status.getStatusCode() >> 200
		1 * response.getEntity() >> entity
		2 * entity.getContentLength() >> (long) expected.length()
		1 * entity.getContent() >> IOUtils.toInputStream(expected, StandardCharsets.UTF_8)
		1 * response.close()
		result.body == expected
		result.statusCode == 200
	}

	void "test execute() http response entity is null"() {
		setup:
		String expected = ""
		when:
		CommunityOperatorService.ProxyResponse result = service.proxy(url)
		then:
		1 * service.client.execute(_ as HttpGet) >> response
		1 * response.getStatusLine() >> status
		1 * status.getStatusCode() >> 400
		1 * response.getEntity() >> null
		1 * response.close()
		result.body == expected
		result.statusCode == 400
	}

	void "test execute() returns 400"() {
		setup:
		String expected = """{"error":"bad community address format"}"""
		when:
		CommunityOperatorService.ProxyResponse result = service.proxy(url)
		then:
		1 * service.client.execute(_ as HttpGet) >> response
		1 * response.getStatusLine() >> status
		1 * status.getStatusCode() >> 400
		1 * response.getEntity() >> entity
		2 * entity.getContentLength() >> (long) expected.length()
		1 * entity.getContent() >> IOUtils.toInputStream(expected, StandardCharsets.UTF_8)
		1 * response.close()
		result.body == expected
		result.statusCode == 400
	}

	void "test community server not responding"() {
		when:
		service.proxy(url)
		then:
		1 * service.client.execute(_ as HttpGet) >> { throw new ConnectException("mocked: server down") }
		def e = thrown(ProxyException)
		e.message == "Community server is not responding"
		e.code == "PROXY_ERROR"
		e.statusCode == 500
	}

	void "test execute() returns 404"() {
		setup:
		String expected = """{"error":"community address not found"}"""
		when:
		CommunityOperatorService.ProxyResponse result = service.proxy(url)
		then:
		1 * service.client.execute(_ as HttpGet) >> response
		1 * response.getStatusLine() >> status
		1 * status.getStatusCode() >> 404
		1 * response.getEntity() >> entity
		2 * entity.getContentLength() >> (long) expected.length()
		1 * entity.getContent() >> IOUtils.toInputStream(expected, StandardCharsets.UTF_8)
		1 * response.close()
		result.body == expected
		result.statusCode == 404
	}

	void "test execute() returns 500"() {
		setup:
		String expected = ""
		when:
		CommunityOperatorService.ProxyResponse result = service.proxy(url)
		then:
		1 * service.client.execute(_ as HttpGet) >> response
		1 * response.getStatusLine() >> status
		1 * status.getStatusCode() >> 500
		1 * response.close()
		result.body == expected
		result.statusCode == 500
	}

	void "test execute() returns unknown status code"() {
		setup:
		String expected = ""
		when:
		CommunityOperatorService.ProxyResponse result = service.proxy(url)
		then:
		1 * service.client.execute(_ as HttpGet) >> response
		1 * response.getStatusLine() >> status
		1 * status.getStatusCode() >> 418
		1 * response.close()
		result.body == expected
		result.statusCode == 418
	}
}
