package com.unifina.controller.api

import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.PermissionService
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import grails.util.GrailsNameUtils

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class PermissionApiController {

	SpringSecurityService springSecurityService
	PermissionService permissionService

	def resource = "modulepackage"
	def id = 1

	private withResource(String resourceType, resourceId, Closure action) {
		def user = springSecurityService.currentUser

		Class resourceClass = grailsApplication.domainClasses*.clazz.find { it.simpleName.toLowerCase() == resourceType }
		if (!resourceClass) { throw new IllegalArgumentException("$resourceClass is not a domain class!") }
		def res = resourceClass.get(resourceId)
		if (!res) { throw new IllegalArgumentException("$resourceType (id $resourceId) not found!") }

		if (!permissionService.canShare(user, res)) {
			render(status: 403, text: "Not authorized to query the Permissions for $resourceType id $resourceId!")
		} else {
			action(res)
		}
	}

	private withPermission(String resourceType, resourceId, Long permissionId, Closure<Permission> action) {
		withResource(resourceType, resourceId) { res ->
			def p = permissionService.getNonOwnerPermissions(null, res).find { it.id == permissionId }
			if (!p) {
				render(status: 404, text: "$resource id $id had no permission with id $permissionId!")
			} else {
				action(p, res)
			}
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def index() {
		withResource(resource, id) { res ->
			def perms = permissionService.getNonOwnerPermissions(null, res).collect {[
				id: it.id,
				user: it.user.username,
				operation: it.operation
			]}
			if (res.user) {
				perms += permissionService.ownerPermissions.collect { [id: null, user: res.user.username, operation: it] }
			}
			render(perms as JSON)
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def show(String permissionId) {
		log.debug("Show $id: ${params.resource} ${params.id}")
		permissionId = params.id		// TODO: this comes from URL
		withPermission(resource, id, permissionId as Long) { p ->
			render([id: p.id, user: p.user.username, operation: p.operation] as JSON)
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def delete(String permissionId) {
		log.debug("Delete $id: ${params.resource} ${params.id}")
		permissionId = params.id		// TODO: this comes from URL
		withPermission(resource, id, permissionId as Long) { p, res ->
			def revoker = springSecurityService.currentUser
			permissionService.revoke(revoker, res, p.user, p.operation)
			render([id: p.id, user: p.user.username, operation: p.operation] as JSON)
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def save() {
		log.debug("Save: ${params.resource} ${params.id}")

		String username = params.id		// TODO: this comes from POST body
		String op = "read"				// TODO: this comes from POST body

		def user = SecUser.findByUsername(username)
		if (!user) {
			render status: 400, text: [text: "User '$username' not found!"] as JSON
		} else if (!permissionService.allOperations.contains(op)) {
			render status: 400, text: [text: "Faulty operation '$op'. Try with 'read', 'write' or 'share' instead."] as JSON
		} else {
			withResource(resource, id) { res ->
				def grantor = springSecurityService.currentUser
				def newP = permissionService.grant(grantor, res, user, op)
				header "Location", "/api/v1/$resource/$id/permissions/${newP.id}"
				render status: 201, text: [text: "Successfully granted", user: username, operation: op] as JSON
			}
		}
	}

}
