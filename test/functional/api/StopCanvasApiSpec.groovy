package api

import com.unifina.domain.signalpath.Canvas
import geb.spock.GebReportingSpec
import grails.plugins.rest.client.ErrorResponse
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import org.springframework.http.HttpStatus

class StopCanvasApiSpec extends GebReportingSpec {
	def canvasId = "kldfaj2309jr9wjf9ashjg9sdgu9"

	def "stopping dead canvas should change state to STOPPED"() {
		when:
		ErrorResponse response = authenticatedPost(baseUrl + "/api/v1/canvases/$canvasId/stop")

		then:
		response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
		response.json != []
		response.json.code == "CANVAS_UNREACHABLE"

		when:
		RestResponse response2 = authenticatedGet(baseUrl + "/api/v1/canvases/$canvasId")

		then:
		response2.json["state"] == Canvas.State.STOPPED.toString()
	}

	def authenticatedPost(String url) {
		new RestBuilder().post(url) {
			header("Authorization", "token tester1-api-key")
		}
	}

	def authenticatedGet(String url) {
		new RestBuilder().get(url) {
			header("Authorization", "token tester1-api-key")
		}
	}
}
