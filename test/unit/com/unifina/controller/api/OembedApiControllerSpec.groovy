package com.unifina.controller.api

import com.unifina.api.ApiException
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(OembedApiController)
class OembedApiControllerSpec extends Specification {

    void "index returns a right kind of json with a valid request"() {
		def url = "https://www.streamr.com/canvas/embed/sghBX0pOR-alVJ09ltnG1QQ_pr9qi4SaWw55El3LV9ig"
		def encodedUrl = URLEncoder.encode(url, "UTF-8")
		when:
		request.method = "GET"
		params.url = encodedUrl
		controller.index()
		then:
		response.status == 200
		response.json.url == url
		response.json.width == 400
		response.json.height == 300
		response.json.provider_name == "Streamr"
		response.json.provider_url == "https://www.streamr.com"
		response.json.type == "rich"
		response.json.version == "1.0"
		response.json.html.matches(/\s*<iframe.*\/>\s*/)
		response.json.html.find(/width="400"/)
		response.json.html.find(/height="300"/)
		response.json.html.find(/src="${url}\"/)
    }

    void "index accepts maxwidth and maxheight as params"() {
		def url = "https://www.streamr.com/canvas/embed/sghBX0pOR-alVJ09ltnG1QQ_pr9qi4SaWw55El3LV9ig"
		def encodedUrl = URLEncoder.encode(url, "UTF-8")
		when:
		request.method = "GET"
		params.url = encodedUrl
		params.maxwidth = "100"
		params.maxheight = "200.5"
		params.url = encodedUrl
		controller.index()
		then:
		response.status == 200
		response.json.url == url
		response.json.width == 100
		response.json.height == 200.5
		response.json.html.find(/width="100"/)
		response.json.html.find(/height="200.5"/)
    }

	void "index accepts the url in http"() {
		def url = "http://www.streamr.com/canvas/embed/a"
		def encodedUrl = URLEncoder.encode(url, "UTF-8")

		when:
		request.method = "GET"
		params.url = encodedUrl
		controller.index()
		then:
		response.status == 200
		response.json.url == url
	}

	void "index accepts the without www"() {
		def url = "https://streamr.com/canvas/embed/a"
		def encodedUrl = URLEncoder.encode(url, "UTF-8")

		when:
		request.method = "GET"
		params.url = encodedUrl
		controller.index()
		then:
		response.status == 200
		response.json.url == url
	}

	void "index fails with an invalid url"() {
		when:
		request.method = "GET"
		params.url = "fafasdfasasadfasdfasdf"
		controller.index()
		then:
		ApiException ex = thrown()
		ex.getStatusCode() == 404
	}

	void "index works with format json"() {
		def url = "https://streamr.com/canvas/embed/a"
		def encodedUrl = URLEncoder.encode(url, "UTF-8")
		when:
		request.method = "GET"
		params.url = encodedUrl
		params.format = "json"
		controller.index()
		then:
		response.status == 200
	}

	void "index works with format JSON"() {
		def url = "https://streamr.com/canvas/embed/a"
		def encodedUrl = URLEncoder.encode(url, "UTF-8")
		when:
		request.method = "GET"
		params.url = encodedUrl
		params.format = "JSON"
		controller.index()
		then:
		response.status == 200
	}

	void "index fails with an invalid format"() {
		def url = "https://streamr.com/canvas/embed/a"
		def encodedUrl = URLEncoder.encode(url, "UTF-8")
		when:
		request.method = "GET"
		params.url = encodedUrl
		params.format = "xml"
		controller.index()
		then:
		ApiException ex = thrown()
		ex.getStatusCode() == 501
	}
}
