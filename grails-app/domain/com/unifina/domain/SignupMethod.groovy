package com.unifina.domain

import com.unifina.utils.ApplicationConfig
import javax.servlet.http.HttpServletRequest

enum SignupMethod {
	API,
	CORE,
	UNKNOWN,
	MIGRATED

	private final static String SERVER_URL_CONFIG_KEY = "grails.serverURL"

	static SignupMethod fromRequest(HttpServletRequest request) {
		String serverUrl = ApplicationConfig.getString(SERVER_URL_CONFIG_KEY)
		String origin = request.getHeader("Origin")
		return (origin == serverUrl) ? SignupMethod.CORE : SignupMethod.API
	}
}
