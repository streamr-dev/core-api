package com.unifina.domain

import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(Permission)
class PermissionSpec extends Specification {
	void "validation fails if no domain class attached"() {
		def permission = new Permission(operation: Permission.Operation.STREAM_GET)
		permission.save()
		expect:
		!permission.validate()
	}

	void "validation fails if more than 1 domain class attached"() {
		def product = new Product()
		def stream = new Stream(name: "stream")
		def permission = new Permission(operation: Permission.Operation.STREAM_GET, product: product, stream: stream)
		expect:
		!permission.validate()
	}

	void "validation succeeds if exactly 1 domain class attached"() {
		def stream = new Stream()
		def permission = new Permission(operation: Permission.Operation.STREAM_GET, stream: stream)
		expect:
		permission.validate()
	}

	void "all items in resourceFields are defined as fields"() {
		Permission p = new Permission(operation: Permission.Operation.STREAM_GET)

		when:
		Permission.resourceFields.each {
			p[it] // throws if field doesn't exist
		}

		then:
		noExceptionThrown()
	}

	void "permission fromString() accepts lowercase or uppercase string"(String input, Permission.Operation result) {
		expect:
		Permission.Operation.fromString(input) == result
		where:
		input         | result
		"product_get" | Permission.Operation.PRODUCT_GET
		"PRODUCT_GET" | Permission.Operation.PRODUCT_GET
		"PrOdUcT_gEt" | Permission.Operation.PRODUCT_GET
	}

	void "permission fromString() throws IllegalArgumentException on invalid input"() {
		when:
		Permission.Operation.fromString(null)
		then:
		thrown(IllegalArgumentException)

		when:
		Permission.Operation.fromString("")
		then:
		thrown(IllegalArgumentException)

		when:
		Permission.Operation.fromString("WRITE")
		then:
		thrown(IllegalArgumentException)
	}
}
