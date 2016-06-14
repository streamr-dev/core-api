package com.unifina.api

class StreamrApiHelper {
	static Closure createListCriteria(def params, List<String> searchFields, Closure additionalCriteria = {}) {
		def result = {
			if (params.search) {
				or {
					searchFields.each {
						like it, "%${params.search}%"
					}
				}
			}
			if (params.sort) {
				order params.sort, params.order ?: "asc"
			}
			if (params.max) {
				maxResults Integer.parseInt(params.max)
			}
			if (params.offset) {
				firstResult Integer.parseInt(params.offset)
			}
		}

		return result << additionalCriteria
	}

	static boolean isPublicFlagOn(params) {
		return params.public != null && Boolean.parseBoolean(params.public)
	}
}
