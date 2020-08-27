package com.unifina.domain

import com.unifina.utils.MapTraversal
import grails.util.Holders

import javax.servlet.http.HttpServletRequest

enum SignupMethod {
	API,
	CORE,
	UNKNOWN

	private final static String SERVER_URL_CONFIG_KEY = "grails.serverURL"

	static SignupMethod fromRequest(HttpServletRequest request) {
		def serverUrl = MapTraversal.getString(Holders.getConfig(), SERVER_URL_CONFIG_KEY)
		def origin = request.getHeader("Origin")
		return (origin == serverUrl) ? SignupMethod.CORE : SignupMethod.API
	}
}
