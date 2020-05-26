package com.unifina.controller.api

import com.unifina.api.ValidationException
import com.unifina.domain.EmailMessage
import com.unifina.domain.Resource
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.security.AllowRole
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.PermissionService
import com.unifina.utils.EthereumAddressValidator
import com.unifina.utils.UsernameValidator
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
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		List<Permission> permissions = permissionService.findAllPermissions(resource, request.apiUser, request.apiKey, subscriptions)
		def perms = permissions*.toMap()
		render(perms as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def getOwnPermissions() {
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		List<Permission> permissions = permissionService.getOwnPermissions(resource, request.apiUser, request.apiKey)
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
		Resource res = new Resource(params.resourceClass, params.resourceId)
		SecUser apiUser = request.apiUser
		Key apiKey = request.apiKey
		Permission newPermission
		if (cmd.anonymous) {
			newPermission = permissionService.saveAnonymousPermission(apiUser, apiKey, op, res)
		} else {
			String subjectTemplate = grailsApplication.config.unifina.email.shareInvite.subject
			String sharer = apiUser?.username
			// incoming "username" is either SecUser.username or SignupInvite.username (possibly of a not yet created SignupInvite)
			SecUser user = SecUser.findByUsername(username)
			if (user) {
				String recipient = user.username
				// send share resource email and grant permission
				EmailMessage msg = new EmailMessage(sharer, recipient, subjectTemplate, res)
				newPermission = permissionService.savePermissionAndSendShareResourceEmail(
					apiUser,
					apiKey,
					op,
					user.username,
					msg
				)
			} else {
				if (EthereumAddressValidator.validate(username)) {
					// get or create a user based on an ethereum account, and grant permission
					newPermission = permissionService.savePermissionForEthereumAccount(username, apiUser, op, res)
				} else {
					// send share resource invite email and grant permission
					EmailMessage msg = new EmailMessage(sharer, username, subjectTemplate, res)
					newPermission = permissionService.savePermissionAndSendEmailShareResourceInvite(apiUser, username, op, msg)
				}
			}
		}
		header("Location", request.forwardURI + "/" + newPermission.id)
		render(newPermission.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def show(Long id) {
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		Permission p = permissionService.findPermission(id, resource, request.apiUser, request.apiKey)
		render(p.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def delete(Long id) {
		Resource resource = new Resource(params.resourceClass, params.resourceId)
		permissionService.deletePermission(id, resource, request.apiUser, request.apiKey)
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

	private static boolean validateOperation(String val) {
		if (val == null) {
			return false
		}
		try {
			Operation.valueOf(val)
		} catch (IllegalArgumentException e) {
			return false
		}
		return true
	}

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
			boolean validOperation = validateOperation(val)
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
