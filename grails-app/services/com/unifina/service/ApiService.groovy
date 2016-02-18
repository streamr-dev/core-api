package com.unifina.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.unifina.api.ApiException
import com.unifina.domain.security.SecUser
import com.unifina.exceptions.UnexpectedApiResponseException
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by henripihkala on 16/02/16.
 */
@CompileStatic
class ApiService {

	private static final Logger log = Logger.getLogger(ApiService)

	Map post(String url, Map body, SecUser user) {
		// TODO: Migrate to Streamr API Java client lib when such a thing is made
		def req = Unirest.post(url)

		if (user)
			req.header("Authorization", "token $user.apiKey")

		req.header("Content-Type", "application/json")

		log.info("request: $body")

		HttpResponse<String> response = req.body((body as JSON).toString()).asString()

		try {
			if (response.getCode()==204)
				return [:]
			else if (response.getCode() >= 200 && response.getCode() < 300) {
				Map responseBody = (JSONObject) JSON.parse(response.getBody())
				return responseBody
			}
			else {
				// JSON error message?
				Map responseBody
				try {
					responseBody = (JSONObject) JSON.parse(response.getBody())
				} catch (Exception e) {
					throw new UnexpectedApiResponseException("Got unexpected response from api call to $url: "+response.getBody())
				}
				throw new ApiException(response.getCode(), responseBody.code?.toString(), responseBody.message?.toString())
			}
		} catch (ConverterException e) {
			log.error("request: Failed to parse JSON response: "+response.getBody())
			throw new RuntimeException("Failed to parse JSON response", e)
		}
	}
}
