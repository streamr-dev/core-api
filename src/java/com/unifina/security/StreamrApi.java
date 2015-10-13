package com.unifina.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * API methods should be use the @StreamrApi annotation and be mapped to /api/* via UnifinaCorePluginUrlMappings.
 * This will allow the UnifinaCoreApiFilters to check user credentials. The authenticated SecUser can be referenced
 * by request.apiUser.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface StreamrApi {
	/**
	 * Set to false if authentication is optional for this API method.
	 */
	boolean requiresAuthentication() default true;
}
