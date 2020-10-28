package com.unifina.controller
import grails.converters.JSON
import com.streamr.client.*;
import com.streamr.client.rest.*;
import com.streamr.client.authentication.*;
import com.streamr.client.dataunion.*;
import com.streamr.client.options.*;
import com.streamr.client.utils.*;
import com.unifina.service.StreamrClientService;

class DevelopmentController {

	StreamrClientService streamrClientService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def foobar() {
		println("FOOBAR")
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode()
		//StreamrClientOptions options = new StreamrClientOptions(new ApiKeyAuthenticationMethod("product-api-tester-key"))
		//options.setRestApiUrl("http://localhost/api/v1")
		//StreamrClient client = new StreamrClient(options)
		DataUnionClient duClient = client.dataUnionClient()
		return render([] as JSON)
	}
}