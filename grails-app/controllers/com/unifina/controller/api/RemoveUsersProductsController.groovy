package com.unifina.controller.api

import com.unifina.security.StreamrApi
import com.unifina.service.ProductService
import com.unifina.security.Secured

class RemoveUsersProductsController {
	ProductService productService

	@StreamrApi
	def index() {
		if (params.username == null) {
			render(status: 400)
			return
		}
		if (!request.apiUser.isAdmin()) {
			render(status: 401)
			return
		}
		productService.removeUsersProducts(params.username)
		render(status: 204)
	}
}
