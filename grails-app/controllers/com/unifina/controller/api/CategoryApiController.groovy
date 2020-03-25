package com.unifina.controller.api

import com.unifina.domain.marketplace.Category
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.converters.JSON

class CategoryApiController {
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index() {
		def categories = Category.listOrderByName()
		render(categories*.toMap() as JSON)
	}
}
