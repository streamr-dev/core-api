package com.unifina.service

import com.unifina.api.*
import com.unifina.domain.*
import com.unifina.domain.Permission.Operation
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
			DashboardItem item = new DashboardItem(it.properties)

			item.id = IdGenerator.getShort()

			dashboard.addToItems(item)
			item.save(failOnError: true)
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

		Set<String> ids = dashboard.items?.collect { it.id } as Set

		validCommand.items.each { SaveDashboardItemCommand it ->
			if (!it.validate()) {
				throw new ValidationException(it.errors)
			}
			DashboardItem item
			if (ids.contains(it.id)) {
				item = DashboardItem.findByDashboardAndId(dashboard, it.id)
				item.setProperties(it.properties)
				ids.remove(it.id)
			} else {
				item = new DashboardItem(it.properties)
				item.id = IdGenerator.getShort()
				dashboard.addToItems(item)
			}
			item.save(failOnError: true)
		}

		ids.collect {
			DashboardItem item = DashboardItem.findByDashboardAndId(dashboard, it)
			dashboard.removeFromItems(item)
			item.delete()
		}

		if (!dashboard.validate()) {
			throw new ValidationException(dashboard.errors)
		}

		dashboard.save(failOnError: true)

		return dashboard
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
	DashboardItem findDashboardItem(String dashboardId, String itemId, User user)
		throws NotFoundException, NotPermittedException {
		return authorizedGetDashboardItem(dashboardId, itemId, user, Permission.Operation.DASHBOARD_GET)
	}

	/**
	 * Unassociate dashboard item from a dashboard, and then delete item.
	 * @param dashboardId dashboard id
	 * @param itemId dashboard item id
	 * @param user current user
	 * @throws NotFoundException when dashboard or dashboard item was not found.
	 * @throws NotPermittedException when dashboard was found but user not permitted to update it
	 */
	void deleteDashboardItem(String dashboardId, String itemId, User user)
		throws NotFoundException, NotPermittedException {
		def dashboard = authorizedGetById(dashboardId, user, Permission.Operation.DASHBOARD_EDIT)
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
	DashboardItem addDashboardItem(String dashboardId, SaveDashboardItemCommand command, User user)
		throws NotFoundException, NotPermittedException, ValidationException {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		def dashboard = authorizedGetById(dashboardId, user, Operation.DASHBOARD_EDIT)
		def item = new DashboardItem(command.properties)

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
	DashboardItem updateDashboardItem(String dashboardId, String itemId, SaveDashboardItemCommand command, User user)
		throws NotFoundException, NotPermittedException, ValidationException {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		def item = authorizedGetDashboardItem(dashboardId, itemId, user, Operation.DASHBOARD_EDIT)
		item.setProperties(command.properties)
		item.save(failOnError: true)

		return item
	}

	@CompileStatic
	DashboardItem authorizedGetDashboardItem(String dashboardId, String itemId, User user, Operation operation) {
		def dashboard = authorizedGetById(dashboardId, user, operation)
		def dashboardItem = dashboard.items?.find { DashboardItem item -> item.id == itemId }
		if (dashboardItem == null) {
			throw new NotFoundException(DashboardItem.simpleName, itemId.toString())
		}
		return dashboardItem
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
	RuntimeRequest buildRuntimeRequest(Map msg, String path, String originalPath = path, User user) {
		RuntimeRequest.PathReader pathReader = RuntimeRequest.getPathReader(path)

		Dashboard dashboard = authorizedGetById(pathReader.readDashboardId(), user, Operation.DASHBOARD_GET)
		Canvas canvas = Canvas.get(pathReader.readCanvasId())
		Integer moduleId = pathReader.readModuleId()

		// Does this Dashboard have an item that corresponds to the given canvas and module?
		// If yes, then the user is authenticated to view that widget by having access to the Dashboard.
		// Otherwise, the user must have access to the Canvas itself.
		DashboardItem item = (DashboardItem) DashboardItem.withCriteria(uniqueResult: true) {
			eq "dashboard", dashboard
			eq "canvas", canvas
			eq "module", moduleId
		}

		if (item) {
			Set<Operation> checkedOperations = new HashSet<>()
			checkedOperations.add(Operation.DASHBOARD_GET)
			RuntimeRequest request = new RuntimeRequest(msg, user, canvas, path.replace("dashboards/$dashboard.id/", ""), path, checkedOperations)
			return request
		} else {
			return signalPathService.buildRuntimeRequest(msg, path.replace("dashboards/$dashboard.id/", ""), path, user)
		}
	}
}
