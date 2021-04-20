package com.unifina.service

import com.unifina.controller.TokenAuthenticator.AuthorizationHeader
import com.unifina.domain.Canvas
import com.unifina.domain.Dashboard
import com.unifina.domain.Permission
import com.unifina.domain.Permission.Operation
import com.unifina.domain.User
import com.unifina.signalpath.RuntimeRequest
import com.unifina.utils.IdGenerator
import groovy.transform.CompileStatic

class DashboardService {

	PermissionService permissionService
	SignalPathService signalPathService

	List<Dashboard> findAllDashboards(User user) {
		return permissionService.get(Dashboard, user, Permission.Operation.DASHBOARD_GET, true) { order "dateCreated", "desc" }
	}

	/**
	 * Find Dashboard by id and authorize that user is permitted to read it.
	 * @param id dashboard id
	 * @param user current user
	 * @return found dashboard
	 * @throws NotFoundException when dashboard was not found.
	 * @throws NotPermittedException when dashboard was found but user not permitted to read it
	 */
	@CompileStatic
	Dashboard findById(String id, User user) throws NotFoundException, NotPermittedException {
		Dashboard dashboard = authorizedGetById(id, user, Permission.Operation.DASHBOARD_GET)
		return dashboard
	}

	/**
	 * Delete Dashboard by id and authorize that user is permitted to do so.
	 * @param id dashboard id
	 * @param user current user
	 * @return found dashboard
	 * @throws NotFoundException when dashboard was not found.
	 * @throws NotPermittedException when dashboard was found but user not permitted to delete it
	 */
	@CompileStatic
	void deleteById(String id, User user) throws NotFoundException, NotPermittedException {
		def dashboard = authorizedGetById(id, user, Permission.Operation.DASHBOARD_DELETE)
		dashboard.delete()
	}

	/**
	 * Create Dashboard by command.
	 *
	 * @param validCommand
	 * @param user
	 * @return
	 */
	Dashboard create(SaveDashboardCommand validCommand, User user) {
		Dashboard dashboard = new Dashboard(validCommand.properties.subMap(["name", "layout"]))

		dashboard.id = IdGenerator.getShort()
		dashboard.save(failOnError: true)

		validCommand.items.each {
			if (!it.validate()) {
				throw new ValidationException(it.errors)
			}
		}
		dashboard.save(failOnError: true)
		permissionService.systemGrantAll(user, dashboard)
		return dashboard
	}

	/**
	 * Update Dashboard by command, and authorize that user is permitted to do so.
	 *
	 * @param validCommand
	 * @param user
	 * @throws NotFoundException when dashboard was not found.
	 * @throws NotPermittedException when dashboard was found but user not permitted to update it
	 * @return
	 */
	Dashboard update(String id, SaveDashboardCommand validCommand, User user) throws NotFoundException, NotPermittedException {
		Dashboard dashboard = authorizedGetById(id, user, Operation.DASHBOARD_EDIT)

		def properties = validCommand.properties.subMap(["name", "layout"])
		dashboard.setProperties(properties)

		if (!dashboard.validate()) {
			throw new ValidationException(dashboard.errors)
		}

		dashboard.save(failOnError: true)

		return dashboard
	}

	@CompileStatic
	Dashboard authorizedGetById(String id, User user, Operation operation) {
		def dashboard = Dashboard.get(id)
		if (dashboard == null) {
			throw new NotFoundException(Dashboard.simpleName, id.toString())
		}
		if (!permissionService.check(user, dashboard, operation)) {
			throw new NotPermittedException(user?.username, Dashboard.simpleName, id.toString())
		}
		return dashboard
	}

	@CompileStatic
	RuntimeRequest buildRuntimeRequest(Map msg, String path, String originalPath = path, User user, AuthorizationHeader authorizationHeader) {
		RuntimeRequest.PathReader pathReader = RuntimeRequest.getPathReader(path)

		Dashboard dashboard = authorizedGetById(pathReader.readDashboardId(), user, Operation.DASHBOARD_GET)
		Canvas canvas = Canvas.get(pathReader.readCanvasId())
		Integer moduleId = pathReader.readModuleId()

		return signalPathService.buildRuntimeRequest(msg, path.replace("dashboards/$dashboard.id/", ""), path, user, authorizationHeader)
	}
}
