package com.unifina.controller.api

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.PermissionService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class KeyApiController {

	PermissionService permissionService

	/**
	 * Execute a Controller action using a domain class with access control ("resource")
	 * Checks Permissions for current user first, and blocks the action if access hasn't been granted
	 * @param action Closure that takes up to one argument: the specified resource
	 */
	private useResource(Class resourceClass, resourceId, boolean requireSharePermission=true, Closure action) {
		if (!resourceClass) { throw new IllegalArgumentException("Missing resource class") }
		if (!grailsApplication.isDomainClass(resourceClass)) { throw new IllegalArgumentException("${resourceClass.simpleName} is not a domain class!") }

		// SecUser operations are always on self
		if (resourceClass == SecUser) {
			action(request.apiUser)
		} else {
			def res = resourceClass.get(resourceId)

			if (!res) {
				throw new NotFoundException(resourceClass.simpleName, resourceId.toString())
			} else if (requireSharePermission && !permissionService.canShare(request.apiUser, res)) {
				throw new NotPermittedException(request?.apiUser?.username, resourceClass.simpleName, resourceId.toString(), "share")
			} else {
				action(res)
			}
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def index() {
		useResource(params.resourceClass, params.resourceId) {res ->
			if (res instanceof SecUser) {
				render res.keys*.toMap() as JSON
			} else {
				Map permissionsByKey = permissionService.getPermissionsTo(res)
						.findAll { it.key != null }
						.groupBy { it.key }

				List keys = permissionsByKey.collect { key, permissions ->
					Map map = key.toMap()
					map["permission"] = permissions.find {
						it.operation == Permission.Operation.WRITE
					}?.operation?.id ?: Permission.Operation.READ.id
					return map
				}

				render keys as JSON
			}
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def save() {
		useResource(params.resourceClass, params.resourceId) {res ->
			Key key = new Key(request.JSON)
			key.user = res instanceof SecUser ? res : null
			key.save(failOnError: true, validate: true)

			Map response = key.toMap()

			String permission = request.JSON?.permission

			if (permission) {
				Permission.Operation operation = Permission.Operation.fromString(permission)

				// Throws if user is not permitted to grant
				permissionService.grant(request.apiUser, res, key, operation, false)

				// If granting write, grant also read
				if (operation == Permission.Operation.WRITE) {
					permissionService.grant(request.apiUser, res, key, Permission.Operation.READ, false)
				}

				response["permission"] = permission
			}

			render(response as JSON)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def delete() {
		Key key = Key.get(params.id)
		if (key == null) {
			throw new NotFoundException(Key.toString(), params.id)
		}

		useResource(params.resourceClass, params.resourceId) { res ->

			// Don't allow deleting the only API key for a user. This is needed internally for directing runtime requests to correct node.
			// TODO: this restriction can be removed if HTTP sessions are offloaded to a global store, eg. Redis
			if (res instanceof SecUser && res.keys.size() == 1) {
				throw new NotPermittedException("The only API key of a user cannot be deleted.")
			}

			if (res instanceof SecUser) {
				res.removeFromKeys(key)
			}

			def query = Permission.where {
				key == key
			}
			query.deleteAll()
			key.delete(flush: true)
			response.status = 204
			render ""
		}
	}

}
