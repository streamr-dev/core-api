package com.unifina.domain

import com.unifina.utils.MapTraversal
import grails.util.Holders

import javax.servlet.http.HttpServletRequest

enum SignupMethod {
	API,
	CORE,
	UNKNOWN,
	MIGRATED

	private final static String SERVER_URL_CONFIG_KEY = "grails.serverURL"

	static SignupMethod fromRequest(HttpServletRequest request) {
		String serverUrl = MapTraversal.getString(Holders.getConfig(), SERVER_URL_CONFIG_KEY)
		String origin = request.getHeader("Origin")
		return (origin == serverUrl) ? SignupMethod.CORE : SignupMethod.API
	}
}
