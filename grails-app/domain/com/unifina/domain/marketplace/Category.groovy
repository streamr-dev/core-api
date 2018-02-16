package com.unifina.domain.marketplace

import groovy.transform.CompileStatic

class Category {
	String id
	String name
	String imageUrl

	static hasMany = [products: Product]

	static constraints = {
		imageUrl(nullable: true)
	}

	static mapping = {
		id generator: 'assigned'
		imageUrl length: 2048
		version false
	}

	@CompileStatic
	Map toMap() {
		return [
			id: id,
			name: name,
			imageUrl: imageUrl
		]
	}
}
