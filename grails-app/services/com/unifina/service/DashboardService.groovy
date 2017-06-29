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
import com.unifina.utils.IdGenerator
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
		dashboard.layout = dashboard.layout ? JSON.parse(dashboard.layout) : ''
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

	/**
	 * Create Dashboard by command.
	 *
	 * @param validCommand
	 * @param user
	 * @return
	 */
	Dashboard create(SaveDashboardCommand validCommand, SecUser user) {
		Dashboard dashboard = new Dashboard(validCommand.properties.subMap(["id", "name", "layout"]))
		if (!dashboard.id) {
			dashboard.id = IdGenerator.get()
		}
		dashboard.user = user
		dashboard.save(failOnError: true)

		validCommand.items.each {
			if (!it.validate()) {
				throw new ValidationException(it.errors)
			}
			DashboardItem item = new DashboardItem(it.properties)
			dashboard.addToItems(item)
			item.save(failOnError: true)
		}
		dashboard.save(failOnError: true)
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
	Dashboard update(SaveDashboardCommand validCommand, SecUser user) throws NotFoundException, NotPermittedException {
		Dashboard dashboard = authorizedGetById(validCommand.id, user, Operation.WRITE)

		def properties = validCommand.properties.subMap(["name", "layout"])
		dashboard.setProperties(properties)

		Set<String> ids = dashboard.items.collect { it.id } as Set

		validCommand.items.each {
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


		return Dashboard.findById(validCommand.id)
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
	DashboardItem findDashboardItem(String dashboardId, String itemId, SecUser user)
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
	void deleteDashboardItem(String dashboardId, String itemId, SecUser user)
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
	DashboardItem updateDashboardItem(String dashboardId, String itemId, SaveDashboardItemCommand command, SecUser user)
		throws NotFoundException, NotPermittedException, ValidationException {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		def item = authorizedGetDashboardItem(dashboardId, itemId, user, Operation.WRITE)
		item.setProperties(command.getProperties())
		item.save(failOnError: true)

		return item
	}

	@CompileStatic
	DashboardItem authorizedGetDashboardItem(String dashboardId, String itemId, SecUser user, Operation operation) {
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
