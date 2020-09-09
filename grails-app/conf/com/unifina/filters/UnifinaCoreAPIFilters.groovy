package com.unifina.filters

import com.unifina.controller.StreamrApi
import com.unifina.controller.AuthenticationResult
import com.unifina.controller.TokenAuthenticator
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

import java.lang.reflect.Method

/**
 * API methods should use the @StreamrApi annotation and be mapped to /api/* via UnifinaCorePluginUrlMappings.
 * This will allow the UnifinaCoreApiFilters to check user credentials. The authenticated User can be referenced
 * by request.apiUser.
 */
class UnifinaCoreAPIFilters {
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
		authenticationFilter(uri: '/api/**', uriExclude: '/api/v1/login/**') {
			before = {
				StreamrApi annotation = getApiAnnotation(controllerName, actionName)

				try {
					TokenAuthenticator authenticator = new TokenAuthenticator()
					AuthenticationResult result = authenticator.authenticate(request)

					if (result.lastAuthenticationMalformed) {
						render(
							status: 400,
							text: [
								code   : "MALFORMED_TOKEN",
								message: "Invalid request. Did you pass a HTTP header of the form 'Authorization: [Token|Bearer] my-key-or-token' ?"
							] as JSON
						)
						return false
					}

					if (!result.guarantees(annotation.authenticationLevel())) {
						render(
							status: 401,
							text: [
								code   : "NOT_AUTHENTICATED",
								message: "Not authenticated via token or cookie"
							] as JSON
						)
						return false
					} else if (!result.hasOneOfRoles(annotation.allowRoles())) {
						render(
							status: 403,
							text: [
								code   : "NOT_PERMITTED",
								message: "Not authorized to access this endpoint"
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
				} catch (Exception e) {
					render(
						status: 500,
						text: [
							code   : "INTERNAL_ERROR",
							message: e.toString()
						] as JSON
					)
					return false
				}

			}
		}

		contentTypeFilter(uri: '/api/**') {
			before = {
				String contentLength = request.getHeader("Content-Length")
				Long len
				try {
					len = Long.parseLong(contentLength)
				} catch (NumberFormatException e) {
					len = 0
				}
				// If the request has a body, check content type
				if (request.method in ["POST", "PUT", "PATCH"] && len > 0) {
					String contentType = request.getHeader("Content-Type")
					// When contentType is "application/json; charset=UTF-8" or "multipart/form-data; boundary=--------------------------388527064590944058251699"
					if (contentType != null && contentType.contains(";")) {
						contentType = contentType.split(";")[0].trim()
					}
					StreamrApi annotation = getApiAnnotation(controllerName, actionName)

					for (String acceptedContentType : annotation.expectedContentTypes()) {
						if (acceptedContentType == contentType) {
							return true
						}
					}
					render(
						status: 415,
						text: [
							code   : "UNEXPECTED_CONTENT_TYPE",
							message: "Unexpected content type on request: ${request.getHeader("Content-Type")}. Expected one of: ${Arrays.toString(annotation.expectedContentTypes())}",
						] as JSON
					)
					return false
				}
				return true
			}
		}
	}
}
