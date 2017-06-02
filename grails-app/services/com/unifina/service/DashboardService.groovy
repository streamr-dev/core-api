package com.unifina.service

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.SaveDashboardCommand
import com.unifina.api.SaveDashboardItemCommand
import com.unifina.api.ValidationException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.signalpath.RuntimeRequest
import grails.converters.JSON
import groovy.transform.CompileStatic

class DashboardService {

	PermissionService permissionService
	SignalPathService signalPathService

	List<Dashboard> findAllDashboards(SecUser user) {
		return permissionService.getAll(Dashboard, user) { order "dateCreated", "desc" }
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
	Dashboard findById(String id, SecUser user) throws NotFoundException, NotPermittedException {
		Dashboard dashboard = authorizedGetById(id, user, Permission.Operation.READ)
		dashboard.layout = JSON.parse(dashboard.layout)
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
	void deleteById(String id, SecUser user) throws NotFoundException, NotPermittedException {
		def dashboard = authorizedGetById(id, user, Permission.Operation.WRITE)
		dashboard.delete()
	}

	private saveDashboardAndItems(Dashboard dashboard, items) {
		dashboard.save(failOnError: true)

		items.each { item ->
			item.dashboard = null
			DashboardItem dashboardItem = new DashboardItem(item)
			dashboardItem.id = item.id
			dashboard.addToItems(dashboardItem)
		}

		dashboard
	}

	/**
	 * Create Dashboard by command, and authorize that user is permitted to do so.
	 *
	 * @param id (unused)
	 * @param validCommand
	 * @param user
	 * @return
	 */
	Dashboard create(Map json, SecUser user) {
		def items = []
		if (json.items != null) {
			items = json.items.clone()
			json.items.clear()
		}

		Map dbMap = json as Map
		dbMap << [user: user]
		def id = dbMap.id
		dbMap.remove("id")
		Dashboard dashboard = new Dashboard(dbMap)
		dashboard.id = id

		return saveDashboardAndItems(dashboard, items)
	}

	/**
	 * Update Dashboard by command, and authorize that user is permitted to do so.
	 *
	 * @param id (unused)
	 * @param validCommand
	 * @param user
	 * @return
	 */
	Dashboard update(Map json, SecUser user) {
		Dashboard dashboard = authorizedGetById(json.id, user, Permission.Operation.WRITE)
		dashboard.name = json.name
		dashboard.layout = json.layout

		def items = []
		if (json.items != null) {
			items = json.items.clone()
			json.items.clear()
		}

		return saveDashboardAndItems(dashboard, items)
	}

	/**
	 * Find DashboardItem by (parent) dashboard id and item id, and authorize that user is permitted to read it.
	 *
	 * @param dashboardId dashboard it
	 * @param itemId dashboard item id
	 * @param user current user
	 * @return
	 * @throws NotFoundException either dashboard or dashboard item (under dashboard) was not found
	 * @throws NotPermittedException not permitted to read dashboard
	 */
	@CompileStatic
	DashboardItem findDashboardItem(String dashboardId, Long itemId, SecUser user)
		throws NotFoundException, NotPermittedException {
		return authorizedGetDashboardItem(dashboardId, itemId, user, Permission.Operation.READ)
	}

	/**
	 * Unassociate dashboard item from a dashboard, and then delete item.
	 * @param dashboardId dashboard id
	 * @param itemId dashboard item id
	 * @param user current user
	 * @throws NotFoundException when dashboard or dashboard item was not found.
	 * @throws NotPermittedException when dashboard was found but user not permitted to update it
	 */
	void deleteDashboardItem(String dashboardId, Long itemId, SecUser user)
		throws NotFoundException, NotPermittedException {
		def dashboard = authorizedGetById(dashboardId, user, Permission.Operation.WRITE)
		def dashboardItem = dashboard.items.find { DashboardItem item -> item.id == itemId }
		if (dashboardItem == null) {
			throw new NotFoundException(DashboardItem.simpleName, itemId.toString())
		}
		dashboard.removeFromItems(dashboardItem)
		dashboardItem.delete()
	}

	/**
	 * Create a dashboard item and associate it with a dashboard.
	 * @param dashboardId dashboard to associate with
	 * @param command the command for creating the dashboard item
	 * @param user current user
	 * @return created dashboard item
	 * @throws NotFoundException when dashboard not found
	 * @throws NotPermittedException when not permitted to add item to dashboard
	 * @throws ValidationException when command object is not valid
	 */
	DashboardItem addDashboardItem(String dashboardId, SaveDashboardItemCommand command, SecUser user)
		throws NotFoundException, NotPermittedException, ValidationException {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		def dashboard = authorizedGetById(dashboardId, user, Operation.WRITE)
		def item = command.toDashboardItem()
		dashboard.addToItems(item)
		dashboard.save(failOnError: true)
		return item
	}

	/**
	 * Update an existing dashboard item.
	 * @param dashboardId (parent) dashboard id
	 * @param itemId dashboard item id
	 * @param command the command for updating the dashboard item
	 * @param user current user
	 * @return updated dashboard item
	 * @throws NotFoundException when dashboard or dashboard item not found
	 * @throws NotPermittedException when not permitted to update dashboard item
	 * @throws ValidationException when command object is not valid
	 */
	@CompileStatic
	DashboardItem updateDashboardItem(String dashboardId, Long itemId, SaveDashboardItemCommand command, SecUser user)
		throws NotFoundException, NotPermittedException, ValidationException {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		def item = authorizedGetDashboardItem(dashboardId, itemId, user, Operation.WRITE)
		command.copyValuesTo(item)
		item.save(failOnError: true)

		return item
	}

	@CompileStatic
	DashboardItem authorizedGetDashboardItem(String dashboardId, Long itemId, SecUser user, Operation operation) {
		def dashboard = authorizedGetById(dashboardId, user, operation)
		def dashboardItem = dashboard.items?.find { DashboardItem item -> item.id == itemId }
		if (dashboardItem == null) {
			throw new NotFoundException(DashboardItem.simpleName, itemId.toString())
		}
		return dashboardItem
	}

	@CompileStatic
	Dashboard authorizedGetById(String id, SecUser user, Operation operation) {
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
	RuntimeRequest buildRuntimeRequest(Map msg, String path, String originalPath = path, SecUser user) {
		RuntimeRequest.PathReader pathReader = RuntimeRequest.getPathReader(path)

		String dashboardId = pathReader.readDashboardId()

		Dashboard dashboard
		DashboardItem item
		Canvas canvas

		try {
			dashboard = authorizedGetById(dashboardId, user, Operation.READ)
		} catch (NotFoundException ignored) {}


		// Does this Dashboard have an item that corresponds to the given canvas and module?
		// If yes, then the user is authenticated to view that widget by having access to the Dashboard.
		// Otherwise, the user must have access to the Canvas itself.

		if (dashboard) {
			canvas = Canvas.get(pathReader.readCanvasId())
			Integer moduleId = pathReader.readModuleId()
			item = (DashboardItem) DashboardItem.withCriteria(uniqueResult: true) {
				eq("dashboard", dashboard)
				eq("canvas", canvas)
				eq("module", moduleId)
			}
		}

		if (item) {
			Set<Operation> checkedOperations = new HashSet<>()
			checkedOperations.add(Operation.READ)
			RuntimeRequest request = new RuntimeRequest(msg, user, canvas, path.replace("dashboards/$dashboard.id/", ""), path, checkedOperations)
			return request
		} else {
			return signalPathService.buildRuntimeRequest(msg, path.replace("dashboards/$dashboardId/", ""), path, user)
		}
	}
}
