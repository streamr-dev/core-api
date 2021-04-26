package com.unifina.controller

import grails.converters.JSON
import grails.util.Holders

class NodeApiController {
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def config() {
		Map<String, Object> config = Holders.getFlatConfig()

		// Clean up the config from values that the JSON marshaller won't support by calling toString() on them
		config.keySet().each { String key ->
			def value = config.get(key)
			if (!(value instanceof Number
				|| value instanceof String
				|| value instanceof Boolean
				|| value instanceof Collection
				|| value instanceof Map)) {
				config.put(key, value?.toString())
			}
		}
		render(config as JSON)
	}
}
