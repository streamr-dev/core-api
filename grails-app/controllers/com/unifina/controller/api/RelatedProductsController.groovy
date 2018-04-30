package com.unifina.controller.api

import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.ProductService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class RelatedProductsController {
	ProductService productService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
    def index() {
		def related = productService.relatedProducts((String) params.id)
		render(related as JSON)
	}
}
