package com.unifina.controller

import com.unifina.domain.Permission
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
		testName          | user                                         | operation    | expected
		"happy"           | "0x0000000000000000000000000000000000000001" | "STREAM_GET" | true
		"op lowercase"    | "0x0000000000000000000000000000000000000001" | "stream_get" | true
		"wrong operation" | "0x0000000000000000000000000000000000000001" | "xxxx"       | false
		"null user"       | null                                         | "STREAM_GET" | false
		"null operation"  | "0x0000000000000000000000000000000000000001" | null         | false
		"all null"        | null                                         | null         | false
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
		testName         | anonymous | operation    | expected
		"happy"          | true      | "STREAM_GET" | true
		"null anonymoys" | null      | "STREAM_GET" | false
		"null operation" | true      | null         | false
		"all null"       | null      | null         | false
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
		testName                     | user                                         | anonymous | operation    | expected
		"happy"                      | "0x0000000000000000000000000000000000000001" | false     | "STREAM_GET" | true
		"anonymous and user defined" | "0x0000000000000000000000000000000000000001" | true      | "STREAM_GET" | false
		"only operation defined"     | null                                         | null      | "STREAM_GET" | false
		"only null"                  | null                                         | null      | null         | false
	}
}
