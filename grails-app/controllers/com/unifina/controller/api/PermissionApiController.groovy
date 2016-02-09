package com.unifina.controller.api

import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SignupInvite
import com.unifina.security.StreamrApi
import com.unifina.service.PermissionService
import com.unifina.service.SignupCodeService
import grails.converters.JSON
import grails.plugin.mail.MailService
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class PermissionApiController {

	PermissionService permissionService
	SignupCodeService signupCodeService
	MailService mailService

	/**
	 * Execute a Controller action using a domain class with access control ("resource")
	 * Checks Permissions for current user first, and blocks the action if access hasn't been granted
	 * @param action Closure that takes up to one argument: the specified resource
     */
	private useResource(Class resourceClass, resourceId, Closure action) {
		if (!resourceClass) { throw new IllegalArgumentException("Missing resource class") }
		if (!grailsApplication.isDomainClass(resourceClass)) { throw new IllegalArgumentException("${resourceClass.simpleName} is not a domain class!") }

		// TODO: remove kludge when Stream has String id instead of String uuid
		def res = (resourceClass == Stream ? Stream.find { uuid == resourceId } : resourceClass.get(resourceId))
		if (!res) {
			render status: 404, text: [error: "${resourceClass.simpleName} (id $resourceId) not found!", code: "NOTFOUND", fault: resourceClass.simpleName, id: resourceId] as JSON, contentType: "application/json"
		} else if (!permissionService.canShare(request.apiUser, res)) {
			render status: 403, text: [error: "Not authorized to query the Permissions for ${resourceClass.simpleName} $resourceId", code: "FORBIDDEN", fault: "permissions", user: request?.apiUser?.username] as JSON, contentType: "application/json"
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
				render status: 404, text: [error: "${resourceClass.simpleName} $resourceId had no permission with id $permissionId!", code: "NOTFOUND", fault: "permissions"] as JSON, contentType: "application/json"
			} else {
				action(p, res)
			}
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def index() {
		useResource(params.resourceClass, params.resourceId) { res ->
			def perms = permissionService.getPermissionsTo(res)*.toMap()
			render(perms as JSON)
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def save() {
		//log.debug("Grant new permission to ${params.resourceClass.simpleName} ${params.resourceId}")
		String username = request.JSON.user
		String op = request.JSON.operation

		if (!permissionService.getAllOperations().contains(op)) {
			render status: 400, text: [error: "Invalid operation '$op'. Try with 'read', 'write' or 'share' instead.", code: "INVALID", fault: "operation", operation: op] as JSON, contentType: "application/json"
		}

		// TODO: check that username is a valid email address?

		// incoming "username" is either SecUser.username or SignupInvite.username (possibly of a not yet created SignupInvite)
		def user = SecUser.findByUsername(username)
		if (!user) {
			def invite = SignupInvite.findByUsername(username)
			if (!invite) {
				invite = signupCodeService.create(username)
				def sharer = request.apiUser?.username ?: "A friend"    // TODO: get default from config?
				// TODO: react to MailSendException if invite.username is not valid a e-mail address
				// 			containing a com.sun.mail.smtp.SMTPAddressFailedException: 553 5.1.2 The recipient address <derp> is not a valid RFC-5321 address. ll9sm33538336wjc.29 - gsmtp
				//			from SMTP server response
				mailService.sendMail {
					from grailsApplication.config.unifina.email.sender
					to invite.username
					subject grailsApplication.config.unifina.email.shareInvite.subject.replace("%USER%", sharer)
					html g.render(
							template: "/register/email_share_invite",
							model: [invite: invite, sharer: sharer],
							plugin: "unifina-core"
					)
				}
				invite.sent = true
				invite.save()
			}

			// permissionService handles SecUsers and SignupInvitations equally
			user = invite
		}

		useResource(params.resourceClass, params.resourceId) { res ->
			def grantor = request.apiUser
			def newP = permissionService.grant(grantor, res, user, op)
			header "Location", request.forwardURI + "/" + newP.id
			render status: 201, text: newP.toMap() + [text: "Successfully granted"] as JSON, contentType: "application/json"
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def show(String id) {
		usePermission(params.resourceClass, params.resourceId, id as Long) { p, res ->
			render status: 200, text: p.toMap() as JSON, contentType: "application/json"
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def delete(String id) {
		//log.debug("Delete permission ${params.id} of ${params.resourceClass.simpleName} ${params.resourceId}")
		usePermission(params.resourceClass, params.resourceId, id as Long) { p, res ->
			def revoker = request.apiUser
			permissionService.revoke(revoker, res, p.user, p.operation)
			// https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7 says DELETE may return "an entity describing the status", that is:
			def newPerms = permissionService.getPermissionsTo(res)*.toMap()
			render status: 200, text: p.toMap() + [text: "Successfully revoked", changedPermissions: newPerms] as JSON, contentType: "application/json"
			// it's also possible to send no body at all
			//render status: 204
		}
	}


}
