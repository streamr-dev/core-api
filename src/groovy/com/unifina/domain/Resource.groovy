package com.unifina.domain

import groovy.transform.ToString

@ToString
class Resource {
	Class<?> clazz
	Object id

	Resource(Class<?> clazz, Object id) {
		this.clazz = clazz
		this.id = id
	}
	String idToString() {
		return this?.id?.toString()
	}
}
