package com.unifina.controller

import com.unifina.domain.Key
import com.unifina.domain.User
import grails.converters.JSON

class MockAPIFilters {

	// Hack: instance variables didn't work. ThreadLocals are defending against tests run in parallel.
	static ThreadLocal<User> user = new ThreadLocal<>()
 	static ThreadLocal<Key> apiKey = new ThreadLocal<>()

	static setUser(User u) {
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
