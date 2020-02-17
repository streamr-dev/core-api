package com.unifina.controller.api

import com.unifina.api.InvalidArgumentsException
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.AllowRole
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.service.PermissionService
import com.unifina.service.SignupCodeService
import com.unifina.utils.EmailValidator
import com.unifina.utils.EthereumAddressValidator
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class PermissionApiController {

	PermissionService permissionService
	SignupCodeService signupCodeService
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	def mailService

	/**
	 * Execute a Controller action using a domain class with access control ("resource")
	 * Checks Permissions for current user first, and blocks the action if access hasn't been granted
	 * @param action Closure that takes up to one argument: the specified resource
     */
	private useResource(Class resourceClass, resourceId, boolean requireSharePermission=true, Closure action) {
		if (!resourceClass) { throw new IllegalArgumentException("Missing resource class") }
		if (!grailsApplication.isDomainClass(resourceClass)) { throw new IllegalArgumentException("${resourceClass.simpleName} is not a domain class!") }

		def res = resourceClass.get(resourceId)
		if (!res) {
			throw new NotFoundException(resourceClass.simpleName, resourceId.toString())
		} else if (requireSharePermission && !permissionService.canShare(request.apiUser ?: request.apiKey, res)) {
			throw new NotPermittedException(request?.apiUser?.username, resourceClass.simpleName, resourceId.toString(), "share")
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
		useResource(params.resourceClass, params.resourceId) { res ->
			def perms = permissionService.getPermissionsTo(res)*.toMap()
			render(perms as JSON)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def getOwnPermissions() {
		useResource(params.resourceClass, params.resourceId, false) { res ->
			def perms = permissionService.getPermissionsTo(res, request.apiUser ?: request.apiKey)*.toMap()
			render(perms as JSON)
		}
	}

	private String resource(Class<?> resourceClass) {
		if (Canvas.isAssignableFrom(resourceClass)) {
			return "canvas"
		} else if (Stream.isAssignableFrom(resourceClass)) {
			return "stream"
		} else if (Dashboard.isAssignableFrom(resourceClass)) {
			return "dashboard"
		}
		throw new IllegalArgumentException("Unexpected resource class: " + resourceClass)
	}

	private String resourceName(Class<?> resourceClass, resourceId) {
		def res = resourceClass.get(resourceId)
		if (!res) {
			return ""
		}
		return res.name
	}

	private String emailSubject(String sharer, String resource) {
		String subject = grailsApplication.config.unifina.email.shareInvite.subject
		return subject.replace("%USER%", sharer).replace("%RESOURCE%", resource)
	}

	private String link(Class<?> resourceClass, resourceId) {
		if (Canvas.isAssignableFrom(resourceClass)) {
			return "/canvas/editor/" + resourceId
		} else if (Stream.isAssignableFrom(resourceClass)) {
			return "/core/stream/show/" + resourceId
		} else if (Dashboard.isAssignableFrom(resourceClass)) {
			return "/dashboard/editor/" + resourceId
		}
		throw new IllegalArgumentException("Unexpected resource class: " + resourceClass)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def save() {
		if (!request.hasProperty("JSON")) {
			throw new InvalidArgumentsException("JSON body expected")
		}

		// request.JSON.user is either SecUser.username or SignupInvite.username (possibly of a not yet created SignupInvite)
		boolean anonymous = request.JSON.anonymous as boolean
		String username = request.JSON.user
		if (anonymous && username) { throw new InvalidArgumentsException("Can't specify user for anonymous permission! Leave out either 'user' or 'anonymous' parameter.", "anonymous", anonymous as String) }
		if (!anonymous && !username) { throw new InvalidArgumentsException("Must specify either 'user' or 'anonymous'!") }

		Operation op = Operation.fromString request.JSON.operation
		if (!op) { throw new InvalidArgumentsException("Invalid operation '$opId'. Try e.g. 'read' instead.", "operation", opId) }

		if (anonymous) {
			useResource(params.resourceClass, params.resourceId) { res ->
				def grantor = request.apiUser
				def newP = permissionService.grantAnonymousAccess(grantor, res, op)
				header "Location", request.forwardURI + "/" + newP.id
				response.status = 201
				render(newP.toMap() + [text: "Successfully granted"] as JSON)
			}
		} else {
			// incoming "username" is either SecUser.username or SignupInvite.username (possibly of a not yet created SignupInvite)
			def user = SecUser.findByUsername(username)

			if (user) {
				if (op == Operation.READ) { // quick fix for sending only one email
					if (EmailValidator.validate(user.username)) {
						String sharer = request.apiUser?.username ?: "Streamr user"
						String resource = resource(params.resourceClass)
						String name = resourceName(params.resourceClass, params.resourceId)
						String emailSubject = emailSubject(sharer, resource)
						String link = link(params.resourceClass, params.resourceId)
						mailService.sendMail {
							from grailsApplication.config.unifina.email.sender
							to user.username
							subject emailSubject
							html g.render(
								template: "/emails/email_share_resource",
								model: [
									sharer  : sharer,
									resource: resource,
									name    : name,
									link    : link,
								],
								plugin: "unifina-core"
							)
						}
					}
				}
			} else {
				if (EthereumAddressValidator.validate(username)) {
					user = ethereumIntegrationKeyService.createEthereumUser(username)
				} else {
					def invite = SignupInvite.findByUsername(username)
					if (!invite) {
						invite = signupCodeService.create(username)
						String sharer = request.apiUser?.username ?: "Streamr user"
						String resource = resource(params.resourceClass)
						String name = resourceName(params.resourceClass, params.resourceId)
						String emailSubject = emailSubject(sharer, resource)
						mailService.sendMail {
							from grailsApplication.config.unifina.email.sender
							to invite.username
							subject emailSubject
							html g.render(
								template: "/emails/email_share_resource_invite",
								model: [
									invite: invite,
									sharer: sharer,
									resource: resource,
									name: name,
								],
								plugin: "unifina-core"
							)
						}
						invite.sent = true
						invite.save()
					}

					// permissionService handles SecUsers and SignupInvitations equally
					user = invite
				}
			}

			useResource(params.resourceClass, params.resourceId) { res ->
				def grantor = request.apiUser
				def newP = permissionService.grant(grantor, res, user, op)
				header "Location", request.forwardURI + "/" + newP.id
				response.status = 201
				render(newP.toMap() as JSON)
			}
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
