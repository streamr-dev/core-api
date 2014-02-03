package com.unifina.security

import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*

import java.security.CodeSource
import java.security.Guard
import java.security.GuardedObject
import java.security.PermissionCollection
import java.security.Permissions
import java.security.Policy
import java.security.acl.Permission;

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class GroovySecurityTests {

    void setUp() {
		Policy.setPolicy(new Policy() {
			@Override
			public PermissionCollection getPermissions(CodeSource codesource) {
				Permissions p = new Permissions()
				p.setReadOnly()
				return p
			}
		})
		System.securityManager = new SecurityManager();
    }

    void tearDown() {
        // Tear down logic here
    }

    void testGuardedObject() {
        GuardedObject go = new GuardedObject("Foo", new Guard() {
			void checkGuard(Object object) throws SecurityException {
				throw new SecurityException("works!")
			}
        })
		
		shouldFail(SecurityException) {
			go.getObject()
        }
		
		shouldFail {
			go.object
		}
		
		shouldFail {
			go.@object
		}

    }
}
