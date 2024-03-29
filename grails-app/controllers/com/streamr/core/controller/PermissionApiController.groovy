package com.streamr.core.controller

import com.streamr.core.domain.*
import com.streamr.core.domain.Permission.Operation
import com.streamr.core.service.PermissionService
import com.streamr.core.service.ValidationException
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.ToString

class PermissionApiController {
	PermissionService permissionService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index() {
		boolean subscriptions = true
		if (params.subscriptions) {
			subscriptions = Boolean.parseBoolean(params.subscriptions)
		}
		Resource resource = new Resource(params.productId)
		List<Permission> permissions = permissionService.findAllPermissions(resource, request.apiUser, subscriptions)
		def perms = permissions*.toMap()
		render(perms as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def getOwnPermissions() {
		Resource resource = new Resource(params.productId)
		List<Permission> permissions = permissionService.getOwnPermissions(resource, request.apiUser)
		def perms = permissions*.toMap()
		render(perms as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def save(NewPermissionCommand cmd) {
		if (!cmd.validate()) {
			throw new ValidationException(cmd.errors)
		}
		String username = cmd.user
		Operation op = cmd.operationToEnum()
		Resource res = new Resource(params.productId)
		User apiUser = request.apiUser
		Permission newPermission
		if (cmd.anonymous) {
			newPermission = permissionService.saveAnonymousPermission(apiUser, op, res)
		} else {
			User user = User.findByUsername(username)
			if (user) {
				newPermission = permissionService.savePermission(apiUser, op, user.username, res)
			} else {
				if (EthereumAddressValidator.validate(username)) {
					String origin = request.getHeader("Origin")
					SignupMethod signupMethod = SignupMethod.fromOriginURL(origin)
					newPermission = permissionService.savePermissionForEthereumAccount(username, apiUser, op, res, signupMethod)
				} else {
					newPermission = permissionService.savePermission(apiUser, op, username, res)
				}
			}
		}
		header("Location", request.forwardURI + "/" + newPermission.id)
		render(newPermission.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def show(Long id) {
		Resource resource = new Resource(params.productId)
		Permission p = permissionService.findPermission(id, resource, request.apiUser)
		render(p.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def delete(Long id) {
		Resource resource = new Resource(params.productId)
		permissionService.deletePermission(id, resource, request.apiUser)
		render(status: 204)
	}

	@StreamrApi(allowRoles = AllowRole.DEVOPS)
	def cleanup() {
		permissionService.cleanUpExpiredPermissions()
		render(status: 200)
	}
}

@Validateable
@ToString
class NewPermissionCommand {
	Boolean anonymous
	String user
	String operation

	def beforeValidate() {
		if (operation != null) {
			operation = operation.toUpperCase()
		}
	}

	Operation operationToEnum() {
		Operation op = Operation.valueOf(operation)
		return op
	}

	static constraints = {
		anonymous(nullable: true)
		user(nullable: true, validator: UsernameValidator.validateUsernameOrNull)
		operation(validator: { String val, NewPermissionCommand cmd ->
			boolean validOperation = Operation.validateOperation(val)
			if (!validOperation) {
				return false
			}
			if (cmd.anonymous && cmd.user) {
				return false
			}
			if (validOperation && (cmd.anonymous || cmd.user)) {
				return true
			}
			return false
		})
	}
}
