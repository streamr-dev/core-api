package com.unifina.api

import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import grails.validation.Validateable

@Validateable
class UpdateProductCommand {
	String name
	String description

	Set<Stream> streams = []

	Category category
	Stream previewStream
	String previewConfigJson

	static constraints = {
		importFrom(Product)
	}
}
