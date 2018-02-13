package com.unifina.domain.marketplace

import groovy.transform.CompileStatic

class Category {
	String id
	String name
	String defaultImageUrl

	static hasMany = [products: Product]

	static constraints = {
		defaultImageUrl(nullable: true)
	}

	static mapping = {
		id generator: 'assigned'
		defaultImageUrl length: 2048
		version false
	}

	@CompileStatic
	Map toMap() {
		return [
		    id: id,
			name: name,
			defaultImageUrl: defaultImageUrl
		]
	}
}
