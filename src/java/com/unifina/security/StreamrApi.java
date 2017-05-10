package com.unifina.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API methods should be use the @StreamrApi annotation and be mapped to /api/* via UnifinaCorePluginUrlMappings.
 * This will allow the UnifinaCoreApiFilters to check user credentials. The authenticated SecUser can be referenced
 * by request.apiUser, or the authenticated Key by request.apiKey.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface StreamrApi {
	/**
	 * Set to false if authentication is optional for this API method.
	 */
	AuthLevel authenticationLevel() default AuthLevel.USER;
}
