package com.unifina.filters

import com.unifina.domain.security.SecUser
import com.unifina.security.AuthenticationResult
import com.unifina.security.StreamrApi
import com.unifina.security.TokenAuthenticator
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

import java.lang.reflect.Method

/**
 * API methods should use the @StreamrApi annotation and be mapped to /api/* via UnifinaCorePluginUrlMappings.
 * This will allow the UnifinaCoreApiFilters to check user credentials. The authenticated SecUser can be referenced
 * by request.apiUser.
 */

class UnifinaCoreAPIFilters {
	def springSecurityService

	GrailsApplication grailsApplication
	
	private Map<String, StreamrApi> apiAnnotationCache = new HashMap<String, StreamrApi>()
	
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

			if (!annotation) {
				throw new RuntimeException("No @StreamrApi annotation for controller $controllerName action $actionName")
			} else {
				apiAnnotationCache.put(key, annotation)
				return annotation
			}
		} else {
			return annotation
		}
	}
	
	def filters = {
		authenticationFilter(uri: '/api/**') {
			before = {
				StreamrApi annotation = getApiAnnotation(controllerName, actionName)

				request.isApiAction = true

				TokenAuthenticator authenticator = new TokenAuthenticator()
				AuthenticationResult result = authenticator.authenticate(request)

				if (result.lastAuthenticationMalformed) {
					render (
						status: 400,
						text: [
							code: "MALFORMED_TOKEN",
							message: "Invalid request. Did you pass a HTTP header of the form 'Authorization: Token apiKey' ?"
						] as JSON
					)
					return false
				}

				// Use cookie-based authentication if api key was not present in header.
				if (result.keyMissing) {
					result = new AuthenticationResult((SecUser) springSecurityService.getCurrentUser())
				}
				if (!result.guarantees(annotation.authenticationLevel())) {
					render (
						status: 401,
						text: [
							code: "NOT_AUTHENTICATED",
							message: "Not authenticated via token or cookie"
						] as JSON
					)
					return false
				} else {
					if (result.secUser) {
						request.apiUser = result.secUser
					} else {
						request.apiKey = result.key
					}
					return true
				}
			}
		}
	}
}
