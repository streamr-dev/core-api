package com.unifina.controller.api

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.domain.EmailMessage
import com.unifina.domain.Resource
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.security.AllowRole
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import com.unifina.utils.EthereumAddressValidator
import com.unifina.utils.UsernameValidator
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.ToString

class PermissionApiController {

	PermissionService permissionService
	StreamService streamService

	/**
	 * Execute a Controller action using a domain class with access control ("resource")
	 * Checks Permissions for current user first, and blocks the action if access hasn't been granted
	 * @param action Closure that takes up to one argument: the specified resource
	 * @deprecated Methods breaks transaction boundaries
     */
	@Deprecated
	private useResource(Class resourceClass, resourceId, boolean requireSharePermission=true, Closure action) {
		if (!resourceClass) { throw new IllegalArgumentException("Missing resource class") }
		if (!grailsApplication.isDomainClass(resourceClass)) { throw new IllegalArgumentException("${resourceClass.simpleName} is not a domain class!") }

		def res
		if (Stream.isAssignableFrom(resourceClass)) {
			res = streamService.getStream(resourceId)
		} else {
			res = resourceClass.get(resourceId)
		}
		if (res == null) {
			throw new NotFoundException(resourceClass.simpleName, resourceId.toString())
		}
		Permission.Operation shareOp = Permission.Operation.shareOperation(res)
		if (requireSharePermission && !permissionService.check(request.apiUser ?: request.apiKey, res, shareOp)) {
			throw new NotPermittedException(request?.apiUser?.username, resourceClass.simpleName, resourceId.toString(), shareOp.id)
		} else {
			action(res)
		}
	}

	/**
	 * Execute a Controller action using a Permission object
	 * Checks Permissions to the resource for current user first, and blocks the action if access hasn't been granted
	 * @param action Closure that takes up to two arguments: Permission object, and the resource that Permission applies to
	 * @deprecated Methods breaks transaction boundaries
     */
	@Deprecated
	private usePermission(Class resourceClass, resourceId, Long permissionId, Closure action) {
		useResource(resourceClass, resourceId) { res ->
			def p = permissionService.getPermissionsTo(res).find { it.id == permissionId }
			if (!p) {
				throw new NotFoundException("permissions", permissionId.toString())
			} else {
				action(p, res)
			}
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index() {
		boolean subscriptions = true
		if (params.subscriptions) {
			subscriptions = Boolean.parseBoolean(params.subscriptions)
		}
		useResource(params.resourceClass, params.resourceId) { res ->
			def perms = permissionService.getPermissionsTo(res, subscriptions, null)*.toMap()
			render(perms as JSON)
		}
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
			String from = grailsApplication.config.unifina.email.sender
			String sharer = apiUser?.username
			// incoming "username" is either SecUser.username or SignupInvite.username (possibly of a not yet created SignupInvite)
			SecUser user = SecUser.findByUsername(username)
			if (user) {
				String recipient = user.username
				// send share resource email and grant permission
				EmailMessage msg = new EmailMessage(sharer, recipient, subjectTemplate, res)
				newPermission = permissionService.savePermissionAndSendShareResourceEmail(
					request.apiUser,
					request.apiKey,
					op,
					user.username,
					msg
				)
			} else {
				if (EthereumAddressValidator.validate(username)) {
					// create local ethereum account and grant permission
					newPermission = permissionService.savePermissionAndCreateEthereumAccount(username, request.apiUser, request.apiKey, op, res)
				} else {
					// send share resource invite email and grant permission
					EmailMessage msg = new EmailMessage(sharer, username, subjectTemplate, res)
					newPermission = permissionService.savePermissionAndSendEmailShareResourceInvite(apiUser, username, op, msg)
				}
			}
		}
		header("Location", request.forwardURI + "/" + newPermission.id)
		response.status = 201
		render(newPermission.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def show(String id) {
		usePermission(params.resourceClass, params.resourceId, id as Long) { p, res ->
			render(p.toMap() as JSON)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def delete(String id) {
		usePermission(params.resourceClass, params.resourceId, id as Long) { p, res ->
			// share-permission has been tested in usePermission (calls useResource)
			permissionService.systemRevoke(p)
			render status: 204
		}
	}

	@StreamrApi(allowRoles = AllowRole.DEVOPS)
	def cleanup() {
		permissionService.cleanUpExpiredPermissions()
		render status: 200
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
