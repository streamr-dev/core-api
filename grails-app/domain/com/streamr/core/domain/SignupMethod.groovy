package com.streamr.core.domain

import com.streamr.core.utils.ApplicationConfig

enum SignupMethod {
	API,
	CORE,
	UNKNOWN,
	MIGRATED

	private final static String SERVER_URL_CONFIG_KEY = "grails.serverURL"

	static SignupMethod fromOriginURL(String originUrl) {
		String serverUrl = ApplicationConfig.getString(SERVER_URL_CONFIG_KEY)
		return (originUrl == serverUrl) ? SignupMethod.CORE : SignupMethod.API
	}
}
