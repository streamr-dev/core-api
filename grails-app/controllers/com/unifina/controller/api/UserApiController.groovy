package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class UserApiController {

	@StreamrApi
	def getUserInfo() {
		throw new ApiException(400, "aflsdakfjsaldfk", "jlkjlkjlkjlkj")
		render(request.apiUser?.toMap() as JSON)
	}
}
