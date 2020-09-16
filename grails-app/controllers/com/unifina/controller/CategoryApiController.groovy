package com.unifina.controller

import com.unifina.domain.Category
import grails.converters.JSON

class CategoryApiController {
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index() {
		def categories = Category.listOrderByName()
		render(categories*.toMap() as JSON)
	}
}
