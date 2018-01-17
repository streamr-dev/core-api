package api

import geb.spock.GebReportingSpec
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import org.springframework.http.HttpStatus


// Kind of stupid to extend from GebReportingSpec because it opens a unnecessary web browser but it may fix test
// reporting problems.
class StartCanvasApiSpec extends GebReportingSpec {

	def canvasId = "jklads9812jlsdf09dfgjoaq"

	def "Resuming canvas returns error if canvas cannot be deserialized"() {
		setup:
		authenticatedPost(baseUrl + "/api/v1/canvases/$canvasId/stop")

		when:
		RestResponse response = authenticatedPost(baseUrl + "/api/v1/canvases/$canvasId/start")

		then:
		response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
		response.json.code == "LOADING_PREVIOUS_STATE_FAILED"
	}

	RestResponse authenticatedPost(String url) {
		new RestBuilder().post(url) {
			header("Authorization", "token tester1-api-key")
		}
	}
}
