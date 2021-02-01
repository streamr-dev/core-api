package com.unifina.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API methods should be use the @StreamrApi annotation and be mapped to /api/* via UrlMappings.
 * This will allow the RESTAPIFilters to check user credentials. The authenticated User can be referenced
 * by request.apiUser.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface StreamrApi {
	/**
	 * Set to false if authentication is optional for this API method.
	 */
	AuthLevel authenticationLevel() default AuthLevel.USER;

	AllowRole[] allowRoles() default AllowRole.NO_ROLE_REQUIRED;

	String[] expectedContentTypes() default { "application/json" };
}
