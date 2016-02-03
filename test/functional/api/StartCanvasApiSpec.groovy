package api

import grails.plugins.rest.client.ErrorResponse
import grails.plugins.rest.client.RestBuilder
import org.springframework.http.HttpStatus
import spock.lang.Specification

class StartCanvasApiSpec extends Specification {

	static final String API_URL = "http://localhost:8081/unifina-core/api/v1/"

	def canvasId = "jklads9812jlsdf09dfgjoaq"

	def "GET /canvases/id/resume returns error if canvas cannot be loaded"() {
		setup:
		authenticatedPost(API_URL + "canvases/$canvasId/stop")

		when:
		ErrorResponse response = (ErrorResponse) authenticatedPost(API_URL + "canvases/$canvasId/start")

		then:
		response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
		response.json.code == "LOADING_PREVIOUS_STATE_FAILED"
	}

	def authenticatedPost(String url) {
		new RestBuilder().post(url) {
			header("Authorization", "token tester1-api-key")
		}
	}
}
