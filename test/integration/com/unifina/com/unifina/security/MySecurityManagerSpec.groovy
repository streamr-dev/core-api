package com.unifina.com.unifina.security

import com.unifina.security.MyPolicy
import com.unifina.security.MySecurityManager
import grails.test.spock.IntegrationSpec

import java.security.Policy

class MySecurityManagerSpec extends IntegrationSpec {

	def "MySecurityManager is installed"() {
		expect:
		System.getSecurityManager() instanceof MySecurityManager
	}

	def "MyPolicy is installed"() {
		expect:
		Policy.getPolicy() instanceof MyPolicy
	}
}
