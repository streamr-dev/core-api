package com.unifina.api

import grails.validation.Validateable
import groovy.transform.ToString

@ToString
@Validateable
class DataUnionSecretCommand {
	String name
	String secret

	static constraints = {
		name(blank: false, minSize: 1, maxSize: 200)
		secret(nullable: true, maxSize: 200)
	}
}
