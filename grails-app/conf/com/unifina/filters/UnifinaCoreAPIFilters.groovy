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
	
	def filters = {
		authenticationFilter(uri: '/api/**') {
			before = {
				StreamrApi annotation = getApiAnnotation(controllerName, actionName)
				
				if (!request.JSON || request.JSON.isEmpty()) {
					render (status:400, text: [success:false, error: "Invalid request. Did you set Content-Type to application/json?"] as JSON)
					return false
				}
				else {
					SecUser user = null
					if (request.JSON?.key && request.JSON?.secret) {
						user = unifinaSecurityService.getUserByApiKey(request.JSON?.key, request.JSON?.secret)
					}
						
					if (!user && annotation.requiresAuthentication()) {
						render (status:401, text: [success:false, error: "authentication error"] as JSON)
						return false
					}
					else {
						request.apiUser = user
						return true
					}
				}
			}
		}
	}
}
