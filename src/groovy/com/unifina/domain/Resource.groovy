package com.unifina.domain

import grails.compiler.GrailsCompileStatic
import groovy.transform.ToString

@ToString
@GrailsCompileStatic
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
