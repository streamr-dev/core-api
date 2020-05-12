package com.unifina.controller.api

import com.unifina.api.InvalidArgumentsException
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.EmailMessage
import com.unifina.domain.Resource
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.security.AllowRole
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.PermissionService
import com.unifina.service.SignupCodeService
import com.unifina.service.StreamService
import com.unifina.utils.EthereumAddressValidator
import com.unifina.utils.UsernameValidator
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.ToString

class PermissionApiController {

	PermissionService permissionService
	SignupCodeService signupCodeService
	StreamService streamService

	/**
	 * Execute a Controller action using a domain class with access control ("resource")
	 * Checks Permissions for current user first, and blocks the action if access hasn't been granted
	 * @param action Closure that takes up to one argument: the specified resource
     */
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
     */
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
		useResource(params.resourceClass, params.resourceId, false) { res ->
			def permissionsTo = permissionService.getPermissionsTo(res, request.apiUser ?: request.apiKey)
			def perms = permissionsTo*.toMap()
			render(perms as JSON)
		}
	}

	@Validateable
	@ToString
	static class NewPermissionCommand {
		Boolean anonymous
		String user
		Operation operation
		static constraints = {
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def save() {
		if (!request.hasProperty("JSON")) {
			throw new InvalidArgumentsException("JSON body expected")
		}

		// request.JSON.user is either SecUser.username or SignupInvite.username (possibly of a not yet created SignupInvite)
		boolean anonymous = request.JSON.anonymous as boolean
		String username = request.JSON.user
		if (!UsernameValidator.validate(username)) {
			throw new InvalidArgumentsException("User field in request JSON is not a valid username (email or Ethereum address).")
		}
		if (anonymous && username) {
			throw new InvalidArgumentsException("Can't specify user for anonymous permission! Leave out either 'user' or 'anonymous' parameter.", "anonymous", anonymous as String)
		}
		if (!anonymous && !username) {
			throw new InvalidArgumentsException("Must specify either 'user' or 'anonymous'!")
		}

		Operation op = Operation.fromString(request.JSON.operation)
		if (!op) {
			throw new InvalidArgumentsException("Invalid operation '$op'.", "operation", op)
		}

		Resource res = new Resource(params.resourceClass, params.resourceId)
		SecUser apiUser = request.apiUser
		if (!grailsApplication.isDomainClass(res.clazz)) {
			throw new InvalidArgumentsException("${res.clazz.simpleName} is not a domain class!")
		}
		if (anonymous) {
			useResource(resourceClass, resourceId) { r ->
				SecUser grantor = apiUser
				Permission newP = permissionService.grantAnonymousAccess(grantor, r, op)
				header "Location", request.forwardURI + "/" + newP.id
				response.status = 201
				render(newP.toMap() + [text: "Successfully granted"] as JSON)
			}
		} else {
			String subjectTemplate = grailsApplication.config.unifina.email.shareInvite.subject
			String from = grailsApplication.config.unifina.email.sender
			String sharer = apiUser?.username
			// incoming "username" is either SecUser.username or SignupInvite.username (possibly of a not yet created SignupInvite)
			SecUser user = SecUser.findByUsername(username)
			Permission newPermission
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
			header("Location", request.forwardURI + "/" + newPermission.id)
			response.status = 201
			render(newPermission.toMap() as JSON)
		}
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

