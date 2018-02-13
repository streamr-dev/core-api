package com.unifina.controller.api

import com.unifina.domain.marketplace.Category
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@GrailsCompileStatic
@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class CategoryApiController {
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index() {
		render(Category.list()*.toMap() as JSON)
	}
}
