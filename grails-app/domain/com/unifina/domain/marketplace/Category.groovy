package com.unifina.domain.marketplace

class Category {
	String id
	String name
	String defaultImageUrl

	static hasMany = [
			products: Product
	]

	static constraints = {
		defaultImageUrl(nullable: true)
	}

	static mapping = {
		id generator: 'assigned'
		defaultImageUrl length: 2048
	}
}
