package com.unifina.filters
import grails.converters.JSON
import groovy.transform.CompileStatic

import java.lang.reflect.Method

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi


/**
 * API methods should be use the @ApiMethod annotation and be mapped to /api/* via UnifinaCorePluginUrlMappings.
 * This will allow the UnifinaCoreApiFilters to check user credentials. The authenticated SecUser can be referenced
 * by request.apiUser.
 */

class UnifinaCoreAPIFilters {
	
	def unifinaSecurityService
	GrailsApplication grailsApplication
	
	private Map<String, StreamrApi> apiAnnotationCache = new HashMap<String, StreamrApi>()
	private static final Logger log = Logger.getLogger(UnifinaCoreAPIFilters.class)
	
	@CompileStatic
	private StreamrApi getApiAnnotation(String controllerName, String actionName) {
		String key = "$controllerName/$actionName"
		StreamrApi annotation = apiAnnotationCache.get(key)
		
		if (!annotation) {
			// TODO: can be made more performant in Grails 2.4+ via direct access to controller/action instance?
			def controllerInstance = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName)

			Class controllerClass = this.getClass().getClassLoader().loadClass(controllerInstance.fullName)
			Method action = controllerClass.getMethod(actionName)
			annotation = action.getAnnotation(StreamrApi)
			
			if (!annotation)
				throw new RuntimeException("No @StreamrApi annotation for controller $controllerName action $actionName")
			else {
				apiAnnotationCache.put(key, annotation)
				return annotation
			}
		}
		else return annotation
	}

	/**
	 * Parses apiKey and apiSecret from Authorization token
	 *
	 * @param s "Authorization: Token a:b"
	 * @return ["a", "b"] on success, null on failure
	 */
	@CompileStatic
	private static String[] parseAuthorizationHeader(String s) {
		s = s?.trim()
		if (s != null && !s.isEmpty()) {
			String[] parts = s.split("\\s+")
			if (parts.length == 2 && parts[0].toLowerCase() == "token") {
				String[] keyParts = parts[1].split(":")
				if (keyParts.length == 2) {
					return keyParts
				}
			}
		}
		return null
	}
	
	def filters = {
		authenticationFilter(uri: '/api/**') {
			before = {
				StreamrApi annotation = getApiAnnotation(controllerName, actionName)

				def result = parseAuthorizationHeader(request.getHeader("Authorization"))
				
				if (!result) {
					render (status: 400, text: [
						success: false,
						error: "Invalid request. Did you pass a HTTP header of the form 'Authorization: Token key:secret' ?"
					] as JSON)
					return false
				}

				SecUser user = null
				if (result[0] && result[1]) {
					user = unifinaSecurityService.getUserByApiKey(result[0], result[1])
				}

				if (!user && annotation.requiresAuthentication()) {
					render (status:401, text: [success:false, error: "authentication error"] as JSON)
					return false
				} else {
					request.apiUser = user
					return true
				}

			}
		}
	}
}
