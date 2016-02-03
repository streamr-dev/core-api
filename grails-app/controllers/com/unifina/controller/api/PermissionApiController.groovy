package com.unifina.controller.api

import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.PermissionService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class PermissionApiController {

	PermissionService permissionService

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
		if (!res) { throw new IllegalArgumentException("${resourceClass.simpleName} (id $resourceId) not found!") }

		if (!permissionService.canShare(request.apiUser, res)) {
			render(status: 403, text: "Not authorized to query the Permissions for ${resourceClass.simpleName} $resourceId")
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
				render status: 404, text: [error: "${resourceClass.simpleName} id $resourceId had no permission with id $permissionId!"] as JSON
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

		def user = SecUser.findByUsername(username)
		if (!user) {
			render status: 400, text: [error: "User '$username' not found!"] as JSON
		} else if (!permissionService.allOperations.contains(op)) {
			render status: 400, text: [error: "Faulty operation '$op'. Try with 'read', 'write' or 'share' instead."] as JSON
		} else {
			useResource(params.resourceClass, params.resourceId) { res ->
				def grantor = request.apiUser
				def newP = permissionService.grant(grantor, res, user, op)
				header "Location", request.forwardURI + "/" + newP.id
				render status: 201, text: newP.toMap() + [text: "Successfully granted"] as JSON
			}
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def show(String id) {
		usePermission(params.resourceClass, params.resourceId, id as Long) { p, res ->
			render status: 200, text: p.toMap() as JSON
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def delete(String id) {
		//log.debug("Delete permission ${params.id} of ${params.resourceClass.simpleName} ${params.resourceId}")
		usePermission(params.resourceClass, params.resourceId, id as Long) { p, res ->
			def revoker = request.apiUser
			permissionService.revoke(revoker, res, p.user, p.operation)
			// https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.7 says DELETE may return "an entity describing the status", that is:
			return index()
			// it's also possible to send no body
			//render status: 204
		}
	}


}
