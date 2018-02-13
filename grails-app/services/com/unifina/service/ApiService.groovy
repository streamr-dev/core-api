package com.unifina.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.unifina.api.ApiException
import com.unifina.api.ListParams
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.exceptions.UnexpectedApiResponseException
import grails.converters.JSON
import grails.gorm.PagedResultList
import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.json.JSONObject

class ApiService {

	static transactional = false

	private static final Logger log = Logger.getLogger(ApiService)

	PermissionService permissionService

	/**
	 * List/(search for) all domain objects readable by user that satisfy given conditions
	 *
	 * @param domainClass Class of domain object
	 * @param listParams conditions for listing
	 * @param apiUser user for which listing is conducted (READ permission is checked)
	 *
	 * @return list of results with pagination information
	 */
	ListResult list(Class domainClass, ListParams listParams, SecUser apiUser) {
		Closure searchCriteria = listParams.createListCriteria()
		Closure userPermissionCriteria = permissionService.createUserPermissionCriteria(
			apiUser,
			Permission.Operation.READ,
			listParams.publicAccess
		)


		def criteria = domainClass.createCriteria()
		PagedResultList results = criteria.list(max: listParams.max, offset: listParams.offset) {
			searchCriteria.delegate = delegate
			userPermissionCriteria.delegate = delegate
			userPermissionCriteria()
			searchCriteria()
		}

		return new ListResult(results, listParams.offset)
	}

	/**
	 * Transforms a set of query params to search criteria. Supports search, sort, and paging. Below
	 * is a list of the parameter names and what they do.
	 *
	 * - search (string): only include results where at least one of the 'searchFields' contains this search string (case-insensitive)
	 * - sort (string): string, name of a field to sort by
	 * - order (string): sort direction, either "asc" or "desc"
	 * - max (int): limits the number of results
	 * - offset (int): start the result set from this index, ignoring the first results
	 *
	 * @param params HTTP query params object
	 * @param searchFields Which String fields to search if the 'search' param is given
	 * @param additionalCriteria Any additional criteria that will be added (AND-condition) to the criteria
	 * @return
	 */
	Closure createListCriteria(params, List<String> searchFields, Closure additionalCriteria = {}) {
		def result = {
			if (params.search) {
				or {
					searchFields.each {
						like it, "%${params.search}%"
					}
				}
			}
			if (params.sort) {
				order params.sort, params.order ?: "asc"
			}
			if (params.max) {
				maxResults Integer.parseInt(params.max)
			}
			if (params.offset) {
				firstResult Integer.parseInt(params.offset)
			}
		}

		return result << additionalCriteria
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
