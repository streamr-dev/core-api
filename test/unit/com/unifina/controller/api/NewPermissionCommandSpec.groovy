package com.unifina.controller.api

import com.unifina.domain.security.Permission
import grails.test.mixin.Mock
import spock.lang.Specification
import spock.lang.Unroll

@Mock(Permission)
class NewPermissionCommandSpec extends Specification {
	@Unroll
	void "test validate user"(String testName, String user, String operation, Boolean expected) {
		setup:
		NewPermissionCommand cmd = new NewPermissionCommand(
			user: user,
			operation: operation,
		)
		expect:
		cmd.validate() == expected
		where:
		testName|user|operation|expected
		"happy"|"username@example.com"|"CANVAS_GET"|true
		"op lowercase"|"username@example.com"|"canvas_get"|true
		"wrong operation"|"username@example.com"|"xxxx"|false
		"null user"|null|"CANVAS_GET"|false
		"null operation"|"username@example.com"|null|false
		"all null"|null|null|false
	}

	@Unroll
	void "test validate anonymous"(String testName, Boolean anonymous, String operation, Boolean expected) {
		setup:
		NewPermissionCommand cmd = new NewPermissionCommand(
			anonymous: anonymous,
			operation: operation,
		)
		expect:
		cmd.validate() == expected
		where:
		testName|anonymous|operation|expected
		"happy"|true|"CANVAS_GET"|true
		"null anonymoys"|null|"CANVAS_GET"|false
		"null operation"|true|null|false
		"all null"|null|null|false
	}

	@Unroll
	void "test validate"(String testName, String user, Boolean anonymous, Permission.Operation operation, Boolean expected) {
		setup:
		NewPermissionCommand cmd = new NewPermissionCommand(
			user: user,
			anonymous: anonymous,
			operation: operation,
		)
		expect:
		cmd.validate() == expected
		where:
		testName|user|anonymous|operation|expected
		"happy"|"username@example.com"|false|"CANVAS_GET"|true
		"anonymous and user defined"|"username@example.com"|true|"CANVAS_GET"|false
		"only operation defined"|null|null|"CANVAS_GET"|false
		"only null"|null|null|null|false
	}
}
