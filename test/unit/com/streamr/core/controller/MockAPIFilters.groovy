package com.streamr.core.controller

import com.streamr.core.domain.User
import grails.converters.JSON

class MockAPIFilters {

	// Hack: instance variables didn't work. ThreadLocals are defending against tests run in parallel.
	static ThreadLocal<User> user = new ThreadLocal<>()

	static setUser(User u) {
		user.set(u)
	}

	def filters = {
		authenticationFilter(uri: '**') {
			before = {
				if (user.get()) {
					request.apiUser = user.get()
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
