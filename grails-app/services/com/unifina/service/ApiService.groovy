package com.unifina.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.unifina.api.ApiException
import com.unifina.api.ListParams
import com.unifina.api.ValidationException
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.exceptions.UnexpectedApiResponseException
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

import javax.servlet.http.HttpServletResponse

class ApiService {

	static transactional = false

	private static final Logger log = Logger.getLogger(ApiService)

	PermissionService permissionService
	LinkGenerator grailsLinkGenerator

	/**
	 * List/(search for) all domain objects readable by user that satisfy given conditions. Also validates conditions.
	 *
	 * @param domainClass Class of domain object
	 * @param listParams conditions for listing
	 * @param apiUser user for which listing is conducted (READ permission is checked)
	 * @return list of results with pagination information
	 * @throws ValidationException if listParams does not pass validation
	 */
	@GrailsCompileStatic
	<T> List<T> list(Class<T> domainClass, ListParams listParams, SecUser apiUser) throws ValidationException {
		if (!listParams.validate()) {
			throw new ValidationException(listParams.errors)
		}
		Closure searchCriteria = listParams.createListCriteria()
		permissionService.get(domainClass, apiUser, Permission.Operation.READ, listParams.publicAccess, searchCriteria)
	}

	/**
	 * Generate link to more results in API index() methods
	 */
	@GrailsCompileStatic
	void addLinkHintToHeader(ListParams listParams, int numOfResults, Map params, HttpServletResponse response) {
		if (numOfResults == listParams.max) {
			Map paramMap = listParams.toMap() + [offset: listParams.offset + listParams.max]

			String url = grailsLinkGenerator.link(
				controller: params.controller,
				action: params.action,
				absolute: true,
				params: paramMap.findAll { k, v -> v } // remove null valued entries
			)
			response.addHeader("Link", "<${url}>; rel=\"more\"")
		}
	}

	@CompileStatic
	Map post(String url, Map body, Key key) {
		// TODO: Migrate to Streamr API Java client lib when such a thing is made
		def req = Unirest.post(url)

		if (key) {
			req.header("Authorization", "token $key.id")
		}

		req.header("Content-Type", "application/json")

		log.info("request: $body")

		HttpResponse<String> response = req.body((body as JSON).toString()).asString()

		try {
			if (response.getStatus()==204) {
				return [:]
			} else if (response.getStatus() >= 200 && response.getStatus() < 300) {
				Map responseBody = (JSONObject) JSON.parse(response.getBody())
				return responseBody
			} else {
				// JSON error message?
				Map responseBody
				try {
					responseBody = (JSONObject) JSON.parse(response.getBody())
				} catch (Exception e) {
					throw new UnexpectedApiResponseException("Got unexpected response from api call to $url: "+response.getBody())
				}
				throw new ApiException(response.getStatus(), responseBody.code?.toString(), responseBody.message?.toString())
			}
		} catch (ConverterException e) {
			log.error("request: Failed to parse JSON response: "+response.getBody())
			throw new RuntimeException("Failed to parse JSON response", e)
		}
	}
}
