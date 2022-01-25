package com.streamr.core.domain


import grails.test.mixin.Mock
import spock.lang.Specification

@Mock(Permission)
class PermissionSpec extends Specification {
	void "validation succeeds if exactly 1 domain class attached"() {
		Product product = new Product()
		def permission = new Permission(operation: Permission.Operation.PRODUCT_GET, product: product)
		expect:
		permission.validate()
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
