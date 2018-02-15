package com.unifina.service

import com.unifina.api.ProductListParams
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.SecUser
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ProductService)
class ProductServiceSpec extends Specification {
	void "list() delegates to ApiService#list"() {
		def apiService = service.apiService = Mock(ApiService)
		def me = new SecUser(username: "me@streamr.com")

		when:
		service.list(new ProductListParams(max: 5), me)

		then:
		1 * apiService.list(Product, { it.toMap() == new ProductListParams(max: 5).toMap() }, me)
	}
}
