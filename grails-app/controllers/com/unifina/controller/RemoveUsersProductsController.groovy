package com.unifina.controller

import com.unifina.service.ProductService

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
