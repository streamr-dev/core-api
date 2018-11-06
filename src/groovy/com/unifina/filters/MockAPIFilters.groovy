package com.unifina.filters

import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import grails.converters.JSON

class MockAPIFilters {

	// Hack: instance variables didn't work. ThreadLocals are defending against tests run in parallel.
	static ThreadLocal<SecUser> user = new ThreadLocal<>()
 	static ThreadLocal<Key> apiKey = new ThreadLocal<>()

	static setUser(SecUser u) {
		user.set(u)
	}

	static setKey(Key k) {
		apiKey.set(k)
	}

	def filters = {
		authenticationFilter(uri: '**') {
			before = {
				if (user.get()) {
					request.apiUser = user.get()
				} else if (apiKey.get()) {
					request.apiKey = apiKey.get()
				} else {
					render(
						status: 401,
						text: [
							code   : "NOT_AUTHENTICATED",
							message: "Not authenticated via token or cookie"
						] as JSON
					)
					return false
				}

				return true
			}
		}
	}
}
