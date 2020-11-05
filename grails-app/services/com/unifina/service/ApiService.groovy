package com.unifina.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.unifina.controller.TokenAuthenticator.AuthorizationHeader
import com.unifina.domain.Permission
import com.unifina.domain.User
import com.unifina.domain.Userish
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

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
	 * @param apiUser user for which listing is conducted
	 * @return list of results with pagination information
	 * @throws ValidationException if listParams does not pass validation
	 */
	@GrailsCompileStatic
	<T> List<T> list(Class<T> domainClass, ListParams listParams, User apiUser) throws ValidationException {
		if (!listParams.validate()) {
			throw new ValidationException(listParams.errors)
		}
		Closure searchCriteria = listParams.createListCriteria()
        User effectiveUser = listParams.grantedAccess ? apiUser : null
		permissionService.get(domainClass, effectiveUser, listParams.operation, listParams.publicAccess, searchCriteria)
	}

	/**
	 * Generate link to more results in API index() methods
	 */
	@GrailsCompileStatic
	String createPaginationLink(ListParams listParams, int numOfResults, Map params) {
		if (numOfResults == listParams.max) {
			Map<String, Object> paramMap = listParams.toMap()
			Integer offset = listParams.offset + listParams.max
			paramMap.put("offset", offset)

			String url = grailsLinkGenerator.link(
				controller: params.controller,
				action: params.action,
				absolute: true,
				params: paramMap.findAll { k, v -> v } // remove null valued entries
			)
			return "<${url}>; rel=\"more\""
		}
		return null
	}

	/**
	 * Fetch a domain object by id while authorizing that current user has required permission
	 */
	@GrailsCompileStatic
	<T> T authorizedGetById(Class<T> domainClass, String id, Userish currentUser, Permission.Operation operation)
			throws NotFoundException, NotPermittedException {
		T domainObject = getByIdAndThrowIfNotFound(domainClass, id)
		permissionService.verify(currentUser, domainObject, operation)
		return domainObject
	}

	/**
	 * Fetch a domain object by id and throw NotFoundException if not found
	 */
	def <T> T getByIdAndThrowIfNotFound(Class<T> domainClass, String id) throws NotFoundException {
		T domainObject = domainClass.get(id)
		if (domainObject == null) {
			throw new NotFoundException(domainClass.simpleName, id)
		}
		return domainObject
	}

	@CompileStatic
	Map post(String url, Map body, AuthorizationHeader authorizationHeader) {
		def req = Unirest.post(url)

		if (authorizationHeader) {
			req.header("Authorization", authorizationHeader.toString())
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
