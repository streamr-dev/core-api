package com.unifina.security

import grails.test.spock.IntegrationSpec

import java.security.Policy

class MySecurityManagerIntegrationSpec extends IntegrationSpec {

	def "MySecurityManager is installed"() {
		expect:
		System.getSecurityManager() instanceof MySecurityManager
	}

	def "MyPolicy is installed"() {
		expect:
		Policy.getPolicy() instanceof MyPolicy
	}
}
