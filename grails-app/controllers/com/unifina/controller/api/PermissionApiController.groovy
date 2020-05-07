package com.unifina.controller.api

import com.unifina.api.InvalidArgumentsException
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
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
import com.unifina.service.StreamService
import com.unifina.utils.EmailValidator
import com.unifina.utils.EthereumAddressValidator
import com.unifina.utils.UsernameValidator
import grails.converters.JSON

class PermissionApiController {

	PermissionService permissionService
	SignupCodeService signupCodeService
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	StreamService streamService
	def mailService

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

		String resourceClass = params.resourceClass
		String resourceId = params.resourceId
		SecUser apiUser = request.apiUser
		if (anonymous) {
			useResource(resourceClass, resourceId) { res ->
				SecUser grantor = apiUser
				Permission newP = permissionService.grantAnonymousAccess(grantor, res, op)
				header "Location", request.forwardURI + "/" + newP.id
				response.status = 201
				render(newP.toMap() + [text: "Successfully granted"] as JSON)
			}
		} else {
			// incoming "username" is either SecUser.username or SignupInvite.username (possibly of a not yet created SignupInvite)
			SecUser user = SecUser.findByUsername(username)

			String subjectTemplate = grailsApplication.config.unifina.email.shareInvite.subject
			String from = grailsApplication.config.unifina.email.sender
			String sharer = apiUser?.username
			if (user) {
				if (op == Operation.STREAM_GET || op == Operation.CANVAS_GET || op == Operation.DASHBOARD_GET) { // quick fix for sending only one email
					String recipient = user.username
					if (EmailValidator.validate(recipient)) {
						EmailMessage msg = new EmailMessage(sharer, subjectTemplate, resourceClass, resourceId)
						mailService.sendMail {
							from: from
							to: recipient
							subject: msg.subject()
							html: g.render(
								template: "/emails/email_share_resource",
								model: [
									sharer  : msg.sharer,
									resource: msg.resourceType(),
									name    : msg.resourceName(),
									link    : msg.link(),
								],
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
						EmailMessage msg = new EmailMessage(sharer, subjectTemplate, resourceClass, resourceId)
						invite = signupCodeService.create(username)
						mailService.sendMail {
							from: from
							to: invite.username
							subject: msg.subject()
							html: g.render(
								template: "/emails/email_share_resource_invite",
								model: [
									sharer: msg.sharer,
									resource: msg.resourceType(),
									name: msg.resourceName(),
									invite: invite,
								],
							)
						}
						invite.sent = true
						invite.save(failOnError: true, validate: true)
					}

					// permissionService handles SecUsers and SignupInvitations equally
					user = invite
				}
			}

			useResource(resourceClass, resourceId) { res ->
				SecUser grantor = apiUser
				Permission newP = permissionService.grant(grantor, res, user, op)
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

class EmailMessage {
	String sharer
	String subjectTemplate
	Class<?> resourceClass
	Object resourceId

	EmailMessage(String sharer, String subjectTemplate, Class<?> resourceClass, Object resourceId) {
		this.sharer = sharer ?: "Streamr user"
		this.subjectTemplate = subjectTemplate
		this.resourceClass = resourceClass
		this.resourceId = resourceId
	}

	String resourceType() {
		if (Canvas.isAssignableFrom(resourceClass)) {
			return "canvas"
		} else if (Stream.isAssignableFrom(resourceClass)) {
			return "stream"
		} else if (Dashboard.isAssignableFrom(resourceClass)) {
			return "dashboard"
		}
		throw new IllegalArgumentException("Unexpected resource class: " + resourceClass)
	}

	String resourceName() {
		def res = resourceClass.get(resourceId)
		if (!res) {
			return ""
		}
		return res.name
	}

	String subject() {
		String subject = subjectTemplate.replace("%USER%", sharer)
		subject = subject.replace("%RESOURCE%", resourceType())
		return subject
	}

	String link() {
		if (Canvas.isAssignableFrom(resourceClass)) {
			return "/canvas/editor/" + resourceId
		} else if (Stream.isAssignableFrom(resourceClass)) {
			return "/core/stream/show/" + resourceId
		} else if (Dashboard.isAssignableFrom(resourceClass)) {
			return "/dashboard/editor/" + resourceId
		}
		throw new IllegalArgumentException("Unexpected resource class: " + resourceClass)
	}
}
